#!/usr/bin/env python3
"""AI 旅行规划 SSE 流式接口测试（UTF-8，逐块打印）。"""
import json
import sys
import time
import urllib.request

URL = "http://localhost:8082/api/content/ai/plan/stream"
QUERY = sys.argv[1] if len(sys.argv) > 1 else "带老人去北京玩两天，怎么安排？"

body = json.dumps({"query": QUERY}, ensure_ascii=False).encode("utf-8")
req = urllib.request.Request(
    URL, data=body,
    headers={"Content-Type": "application/json; charset=utf-8", "Accept": "text/event-stream"})

start = time.perf_counter()
with urllib.request.urlopen(req, timeout=90) as r:
    print(f"HTTP {r.status}  Content-Type: {r.headers.get('Content-Type')}")
    for raw in r:
        line = raw.decode("utf-8", "replace").rstrip("\n")
        if line.startswith("data:"):
            data = line[5:].strip()
            if data:
                print(f"[{time.perf_counter() - start:.2f}s] {data}")
print(f"总耗时: {time.perf_counter() - start:.2f}s")
