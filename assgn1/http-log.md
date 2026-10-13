# HTTP Log

### Request

curl -i https://jsonplaceholder.typicode.com/posts/1

### Response 

HTTP/2 200
date: Fri, 14 Aug 2026 19:12:34 GMT
content-type: application/json; charset=utf-8
content-length: 292
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"124-yiKdLzqO5gfBrJFrcdJ8Yq0LGnU"
expires: -1
nel: {"report_to":"heroku-nel","response_headers":["Via"],"max_age":3600,"success_fraction":0.01,"failure_fraction":0.1}
pragma: no-cache
report-to: {"group":"heroku-nel","endpoints":[{"url":"https://nel.heroku.com/reports?s=QwcX6nLtJ2b6%2BABQIX1mfYF8MxKJ69sRv8nTGsMQs8o%3D\u0026sid=e11707d5-02a7-43ef-b45e-2cf4d2036f7d\u0026ts=1785189191"}],"max_age":3600}
reporting-endpoints: heroku-nel="https://nel.heroku.com/reports?s=QwcX6nLtJ2b6%2BABQIX1mfYF8MxKJ69sRv8nTGsMQs8o%3D&sid=e11707d5-02a7-43ef-b45e-2cf4d2036f7d&ts=1785189191"
server: cloudflare
vary: Origin, Accept-Encoding
via: 2.0 heroku-router
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 1000
x-ratelimit-remaining: 999
x-ratelimit-reset: 1785189203
age: 9178
accept-ranges: bytes
cf-cache-status: HIT
cf-ray: a2b2459a2962fc43-MRS
alt-svc: h3=":443"

{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
}
```

**Status:** 200 OK — The request was successful and the requested resource was found.

**Content-Type:** application/json — The response is in JSON format.

---

## Request 2

### Request

```bash
curl -i https://jsonplaceholder.typicode.com/posts/2
```

### Response

```text
HTTP/2 200
date: Fri, 14 Aug 2026 20:15:51 GMT
content-type: application/json; charset=utf-8
content-length: 278
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"116-jnDuMpjju89+9j7e0BqkdFsVRjs"
expires: -1
nel: {"report_to":"heroku-nel","response_headers":["Via"],"max_age":3600,"success_fraction":0.01,"failure_fraction":0.1}
pragma: no-cache
report-to: {"group":"heroku-nel","endpoints":[{"url":"https://nel.heroku.com/reports?s=LyXw%2BAUAvcbdjgm%2Bh3QxTcgXZDNNZZ%2BIWYF1rfJxB7U%3D\u0026sid=e11707d5-02a7-43ef-b45e-2cf4d2036f7d\u0026ts=1786724082"}],"max_age":3600}
reporting-endpoints: {"group":"heroku-nel","endpoints":[{"url":"https://nel.heroku.com/reports?s=LyXw%2BAUAvcbdjgm%2Bh3QxTcgXZDNNZZ%2BIWYF1rfJxB7U%3D\u0026sid=e11707d5-02a7-43ef-b45e-2cf4d2036f7d\u0026ts=1786724082"}],"max_age":3600}
server: cloudflare
vary: Origin, Accept-Encoding
via: 2.0 heroku-router
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 1000
x-ratelimit-remaining: 999
x-ratelimit-reset: 1786724110
age: 14468
accept-ranges: bytes
cf-cache-status: HIT
cf-ray: a2b2a249fc3a1d5a-SIN
alt-svc: h3=":443"

{
  "userId": 1,
  "id": 2,
  "title": "qui est esse",
  "body": "est rerum tempore vitae\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\nqui aperiam non debitis possimus qui neque nisi nulla"
}
```

**Status:** 200 OK — The request was successful and the requested resource was found.

**Content-Type:** application/json — The response is in JSON format.

## Request 3

### Request

```bash
curl -i https://jsonplaceholder.typicode.com/users/1
```

### Response

```text
HTTP/2 200
date: Fri, 14 Aug 2026 20:25:14 GMT
content-type: application/json; charset=utf-8
content-length: 509
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"1fd-+2Y3G3w049iSZtw5t1mzSnunngE"
expires: -1
nel: {"report_to":"heroku-nel","response_headers":["Via"],"max_age":3600}
pragma: no-cache
server: cloudflare
vary: Origin, Accept-Encoding
via: 2.0 heroku-router
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 1000
x-ratelimit-remaining: 999
x-ratelimit-reset: 1786733590
age: 5542
accept-ranges: bytes
cf-cache-status: HIT
cf-ray: a2b2b0087853ce27-SIN
alt-svc: h3=":443"

{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874",
    "geo": {
      "lat": "-37.3159",
      "lng": "81.1496"
    }
  },
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org",
  "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
  }
}
```

**Status:** 200 OK — The request was successful and the user resource was found.

**Content-Type:** application/json — The response is in JSON format.


## Request 4

### Request

```bash
curl -i https://jsonplaceholder.typicode.com/comments/1
```

### Response

```text
HTTP/2 200
date: Fri, 14 Aug 2026 20:29:18 GMT
content-type: application/json; charset=utf-8
content-length: 268
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"10c-KJ4I9RM/+33TKdV8CFsIvqsDSP0"
expires: -1
nel: {"report_to":"heroku-nel","response_headers":["Via"],"max_age":3600}
pragma: no-cache
server: cloudflare
vary: Origin, Accept-Encoding
via: 2.0 heroku-router
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 1000
x-ratelimit-remaining: 967
x-ratelimit-reset: 1786692550
age: 12214
accept-ranges: bytes
cf-cache-status: HIT
cf-ray: a2b2b5fe79624591-MRS
alt-svc: h3=":443"

{
  "postId": 1,
  "id": 1,
  "name": "id labore ex et quam laborum",
  "email": "Eliseo@gardner.biz",
  "body": "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\ndolor quam autem quasi\nreiciendis et nam sapiente accusantium"
}
```

**Status:** 200 OK — The request was successful and the comment resource was found.

**Content-Type:** application/json — The response is in JSON format.


## Request 5 — Deliberate Failure

### Request

```bash
curl -i https://jsonplaceholder.typicode.com/posts/999999
```

### Response

```text
HTTP/2 404
date: Fri, 14 Aug 2026 20:31:47 GMT
content-type: application/json; charset=utf-8
content-length: 2
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"2-vyGp6PvFo4RvsFtPoIWeCReyIC8"
expires: -1
nel: {"report_to":"heroku-nel","response_headers":["Via"],"max_age":3600}
pragma: no-cache
server: cloudflare
vary: Origin, Accept-Encoding
via: 2.0 heroku-router
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 1000
x-ratelimit-remaining: 999
x-ratelimit-reset: 1786739530
cf-cache-status: EXPIRED
cf-ray: a2b2b99c4da788ff-SIN
alt-svc: h3=":443"

{}
```

**Status:** 404 Not Found — The requested resource does not exist.

**Content-Type:** application/json — The response is in JSON format.


## Summary

| Request | Resource        | Status        | Result             |
| ------- | --------------- | ------------- | ------------------ |
| 1       | `/posts/1`      | 200 OK        | Successful         |
| 2       | `/posts/2`      | 200 OK        | Successful         |
| 3       | `/users/1`      | 200 OK        | Successful         |
| 4       | `/comments/1`   | 200 OK        | Successful         |
| 5       | `/posts/999999` | 404 Not Found | Deliberate failure |
