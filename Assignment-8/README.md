
# CampusEats Menu Catalogue REST API

## Project Description
This project implements a REST API for the CampusEats Menu Catalogue using Node.js and Express.

## Technologies Used
- Node.js
- Express.js
- HTML
- CSS
- JavaScript

## Routes

| Method | Path | Description |
|--------|------|-------------|
| GET | /items | Get all menu items |
| GET | /items/:id | Get one menu item |
| POST | /items | Create a new menu item |

## Status Codes
- 200: Successful GET request.
- 201: New item successfully created.
- 404: Requested item not found.
- 422: Required field is missing.

## Assignment Questions

### 1. Which CampusEats service did you pick?
I selected the Menu Catalogue service. Its routes are GET /items, GET /items/:id and POST /items.

### 2. Which status codes did you return?
200 for successful GET requests, 201 for successful creation, 404 when an item does not exist, and 422 when required fields are missing.

### 3. Where did you use req.params and req.body?
I used req.params.id to retrieve an item using its ID from the URL. I used req.body to receive the new menu item's information. POST needs express.json() to parse incoming JSON request data.

## How to Run

1. Install Node.js.
2. Run npm install.
3. Run node server.js.
4. Open http://localhost:3000 in your browser.

## Team Members
Anurag Tiwari(Leader) - 20251651028
Mohd Rashid - 20254651057
Niraj Kumar Gupta - 20251651066
Saurabh Singh - 20251651089
