#!/usr/bin/env python3
"""AI 旅行规划接口测试（UTF-8 客户端）。"""
import json
import sys
import urllib.request

URL = "http://localhost:8082/api/content/ai/plan"
QUERY = sys.argv[1] if len(sys.argv) > 1 else "带老人去北京玩两天，怎么安排？"

body = json.dumps({"query": QUERY}, ensure_ascii=False).encode("utf-8")
req = urllib.request.Request(URL, data=body, headers={"Content-Type": "application/json; charset=utf-8"})
with urllib.request.urlopen(req, timeout=90) as r:
    print(r.read().decode("utf-8"))
