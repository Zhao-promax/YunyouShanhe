#!/usr/bin/env python3
"""ES 同步链路测试：保存景点 → outbox → MQ → 消费同步 ES → 检索验证。"""
import json
import time
import urllib.request


def req(url, obj=None):
    data = json.dumps(obj, ensure_ascii=False).encode("utf-8") if obj is not None else None
    headers = {"Content-Type": "application/json; charset=utf-8"} if obj is not None else {}
    r = urllib.request.Request(url, data=data, headers=headers)
    with urllib.request.urlopen(r, timeout=30) as resp:
        return resp.read().decode("utf-8")


scenic = {
    "cityId": 1, "name": "八达岭长城", "category": "历史人文", "bestSeason": "秋",
    "rating": 4.9, "suggestDays": "1天", "intro": "万里长城，世界文化遗产", "status": 1, "sort": 2
}

print("=== 1. 保存景点 ===")
print(req("http://localhost:8082/api/content/scenic", scenic))

print("\n=== 2. 等待 8 秒（outbox 5s 投递 + 消费同步 ES）===")
time.sleep(8)

print("\n=== 3. ES 检索 '长城' ===")
print(req("http://localhost:8082/api/content/search/scenic?keyword=长城"))
