#!/usr/bin/env python3
"""简单 HTTP 读压测：ThreadPoolExecutor + keep-alive（每 worker 复用一条连接），输出 QPS 与延迟分位。"""
import sys
import time
import http.client
import urllib.parse
from concurrent.futures import ThreadPoolExecutor


def worker(host, port, path, n, out):
    conn = http.client.HTTPConnection(host, port, timeout=10)
    lat = []
    err = 0
    for _ in range(n):
        start = time.perf_counter()
        try:
            conn.request("GET", path)
            resp = conn.getresponse()
            resp.read()
            lat.append((time.perf_counter() - start) * 1000)
        except Exception:
            err += 1
            try:
                conn.close()
            except Exception:
                pass
            conn = http.client.HTTPConnection(host, port, timeout=10)
    conn.close()
    out.append((lat, err))


def main():
    url = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8082/api/content/scenic/list"
    concurrency = int(sys.argv[2]) if len(sys.argv) > 2 else 50
    total = int(sys.argv[3]) if len(sys.argv) > 3 else 4000

    p = urllib.parse.urlparse(url)
    host, port = p.hostname, p.port or 80
    path = p.path + ("?" + p.query if p.query else "")

    per = [total // concurrency] * concurrency
    for i in range(total % concurrency):
        per[i] += 1

    out = []
    start = time.perf_counter()
    with ThreadPoolExecutor(max_workers=concurrency) as ex:
        for n in per:
            ex.submit(worker, host, port, path, n, out)
    wall = time.perf_counter() - start

    lat = []
    err = 0
    for l, e in out:
        lat.extend(l)
        err += e
    lat.sort()
    n = len(lat)

    def pct(p):
        if n == 0:
            return 0
        return lat[min(n - 1, int(n * p))]

    print(f"URL: {url}")
    print(f"并发: {concurrency}  总请求: {total}  成功: {n}  错误: {err}")
    print(f"总耗时: {wall:.2f}s  QPS: {n / wall:.1f}")
    if n:
        print(f"平均: {sum(lat) / n:.1f}ms  p50: {pct(0.50):.1f}ms  p90: {pct(0.90):.1f}ms  p99: {pct(0.99):.1f}ms  max: {lat[-1]:.1f}ms")


if __name__ == "__main__":
    main()
