#!/usr/bin/env python3
"""写路径并发压测：按响应 code 分类统计（成功/限流/错误）。keep-alive。"""
import sys
import time
import json
import http.client
import urllib.parse
import threading
import itertools
from concurrent.futures import ThreadPoolExecutor


def main():
    url = sys.argv[1]
    concurrency = int(sys.argv[2])
    total = int(sys.argv[3])
    body = sys.argv[4]
    user_id = sys.argv[5] if len(sys.argv) > 5 else "1"

    p = urllib.parse.urlparse(url)
    host, port = p.hostname, p.port or 80
    path = p.path + ("?" + p.query if p.query else "")

    counters = {"success": 0, "throttled": 0, "other": 0, "error": 0}
    lock = threading.Lock()
    uid_counter = itertools.count(1)

    def worker(n):
        conn = http.client.HTTPConnection(host, port, timeout=10)
        for _ in range(n):
            uid = next(uid_counter)
            try:
                conn.request("POST", path, body=body,
                             headers={"Content-Type": "application/json", "X-User-Id": str(uid)})
                resp = conn.getresponse()
                data = resp.read().decode("utf-8", "ignore")
                try:
                    obj = json.loads(data)
                    code = obj.get("code")
                    msg = obj.get("message", "")
                    if code == 0:
                        key = "success"
                    elif code == 1000:
                        key = "throttled"
                    else:
                        key = "other"
                except Exception:
                    key = "other"
                with lock:
                    counters[key] += 1
            except Exception:
                with lock:
                    counters["error"] += 1
                try:
                    conn.close()
                except Exception:
                    pass
                conn = http.client.HTTPConnection(host, port, timeout=10)
        conn.close()

    per = [total // concurrency] * concurrency
    for i in range(total % concurrency):
        per[i] += 1

    start = time.perf_counter()
    with ThreadPoolExecutor(max_workers=concurrency) as ex:
        for n in per:
            ex.submit(worker, n)
    wall = time.perf_counter() - start

    print(f"URL: {url}")
    print(f"并发: {concurrency}  总请求: {total}  耗时: {wall:.2f}s  QPS: {total / wall:.1f}")
    print(f"成功: {counters['success']}  限流: {counters['throttled']}  其他业务码: {counters['other']}  异常: {counters['error']}")


if __name__ == "__main__":
    main()
