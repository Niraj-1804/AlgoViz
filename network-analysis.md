# Network Analysis 

## Website

I inspected `https://example.com/` using Chrome DevTools → Network.

## Setup

* DevTools Network panel was opened.
* **Disable cache** was enabled.
* The page was reloaded.
* The Network panel was used to inspect the request waterfall.

## Results

* **Request count:** 1
* **Total page size:** 559 B resources
* **Transferred:** 398 B
* **Slowest resource:** `example.com`
* **Slowest resource time:** 34 ms
* **Finish time:** 34 ms
* **3xx/4xx responses:** None
* **Response status:** 200 OK

## Waterfall Analysis

The Network waterfall showed one request for the page. The request to `example.com` completed in 34 ms. Since there was only one request, it was also the slowest resource.

## Status Code Analysis

The page returned **200 OK**, which means the HTTP request was successful. No 3xx redirect responses or 4xx client-error responses were observed during this reload.
