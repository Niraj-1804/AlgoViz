# CampusEats GraphQL — Dishes Service

Team submission template for the GraphQL assignment. This project exposes a small CampusEats menu/dishes service through GraphQL, with MySQL storage and a plain HTML/CSS/JavaScript page.

## Requirements

- Node.js 18 or newer
- MySQL Server (for example, MySQL 8)
- A MySQL account that can create/use the `campus_eats` database
- Git

## How to run

1. Clone/download this repository and open its folder in a terminal.
2. Install packages:

   ```bash
   npm install
   ```

3. Open `db.js` and replace `YOUR_MYSQL_PASSWORD` with your local MySQL root password. Alternatively, set `DB_HOST`, `DB_USER`, `DB_PASSWORD`, and `DB_NAME` environment variables.
4. Open MySQL Workbench, open `schema.sql`, and execute the complete script. It creates the `campus_eats` database, the `dishes` table, and four seed rows.
5. Start the Node server:

   ```bash
   npm start
   ```

6. Open the frontend at **http://localhost:4000**.
7. Optional: open **http://localhost:4000/graphql** to test GraphQL requests in Apollo Sandbox (if available in your Apollo Server setup).

## Required GraphQL examples

List dishes:

```graphql
query {
  dishes {
    id
    name
    price
    available
  }
}
```

Add a dish:

```graphql
mutation {
  addDish(input: { name: "Paneer Roll", price: 90 }) {
    id
    name
    price
  }
}
```

Find one dish by ID:

```graphql
query {
  dish(id: "1") {
    id
    name
    price
    available
  }
}
```

## Assignment questions — answers

1. **Which CampusEats service did you pick?**  
   We picked the **Dishes/Menu service**, which lists menu dishes and lets a client add a dish.

2. **Name one field a client can skip and one it cannot. Why use `!`?**  
   A client can skip `available` in its selection set because GraphQL lets a client request only the fields it needs. In the schema, `name: String!` cannot be null in a valid `Dish` object. The `!` marks a field as non-null; it does not mean the client must request that field in every query. For `addDish`, `name` and `price` are required in `AddDishInput` because they are marked with `!`. `available` is optional and defaults to `true`.

3. **Map it to SQL: which part became `WHERE`, and which resolver ran `INSERT`?**  
   In `dish(id: ID!)`, the requested `id` becomes the SQL condition `WHERE id = ?` in the `Query.dish` resolver. The `Mutation.addDish` resolver runs `INSERT INTO dishes (name, price, available) VALUES (?, ?, ?)` to create a row. The `?` placeholders keep user-provided values separate from SQL text.

## Demonstration video and screenshots

Capture these yourself while the app is running:

1. **Before screenshot:** open `http://localhost:4000` and show the four seeded dishes.
2. **After screenshot:** enter `Paneer Roll` and `90`, click **Add dish**, and show the new row in the table.
3. Record a short video showing the page before, adding the dish, and the updated table. If possible, also show the GraphQL query/mutation or server console.
4. Upload the video and both screenshots to Google Drive, set sharing permissions as required by your instructor, and add the Drive link here.

- Google Drive demonstration video: `PASTE_YOUR_DRIVE_VIDEO_LINK_HERE`
- Before mutation screenshot: `PASTE_YOUR_BEFORE_SCREENSHOT_LINK_HERE`
- After mutation screenshot: `PASTE_YOUR_AFTER_SCREENSHOT_LINK_HERE`

## Team details

Replace this section with the actual team information before submission.

| Roll number | Student name |
|---|---|
| YOUR_ROLL_NUMBER | YOUR_NAME |
| TEAMMATE_ROLL_NUMBER | TEAMMATE_NAME |

## GitHub submission checklist

- [ ] Push all project files to your team's GitHub repository.
- [ ] Replace the team placeholders above with all roll numbers and names.
- [ ] Add the Google Drive video and screenshot links.
- [ ] Submit the repository link through Google Classroom.
