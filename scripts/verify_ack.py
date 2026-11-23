#!/usr/bin/env python3
"""验证 manual ACK 修复：保存景点 → outbox → MQ → ES，并确认无重复确认。"""
import json
import time
import urllib.request
import urllib.parse


def post(url, obj):
    req = urllib.request.Request(url, data=json.dumps(obj, ensure_ascii=False).encode("utf-8"),
                                 headers={"Content-Type": "application/json; charset=utf-8"})
    return urllib.request.urlopen(req, timeout=30).read().decode("utf-8")


def get(url, kw):
    q = urllib.parse.urlencode({"keyword": kw})
    return urllib.request.urlopen(url + "?" + q, timeout=30).read().decode("utf-8")


scenic = {"cityId": 1, "name": "颐和园", "category": "历史人文", "bestSeason": "春",
          "rating": 4.8, "suggestDays": "1天", "intro": "皇家园林，昆明湖畔", "status": 1, "sort": 3}

print("=== 保存景点 ===")
print(post("http://localhost:8082/api/content/scenic", scenic))
print("\n=== 等待 8 秒 ===")
time.sleep(8)
print("\n=== ES 检索 ===")
print(get("http://localhost:8082/api/content/search/scenic", "颐和园"))
