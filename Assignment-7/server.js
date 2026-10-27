const fs = require("node:fs");
const path = require("node:path");
const express = require("express");
const cors = require("cors");
const { ApolloServer } = require("@apollo/server");
const { expressMiddleware } = require("@apollo/server/express4");
const { startStandaloneServer } = require("@apollo/server/standalone");
const pool = require("./db");

const typeDefs = fs.readFileSync(path.join(__dirname, "schema.graphql"), "utf8");

const resolvers = {
  Query: {
    dishes: async () => {
      const [rows] = await pool.execute(
        "SELECT id, name, price, available FROM dishes ORDER BY id"
      );
      return rows.map(row => ({ ...row, available: Boolean(row.available), price: Number(row.price) }));
    },
    dish: async (_, { id }) => {
      const [rows] = await pool.execute(
        "SELECT id, name, price, available FROM dishes WHERE id = ?",
        [id]
      );
      if (rows.length === 0) return null;
      const row = rows[0];
      return { ...row, available: Boolean(row.available), price: Number(row.price) };
    }
  },
  Mutation: {
    addDish: async (_, { input }) => {
      const available = input.available === undefined ? true : input.available;
      const [result] = await pool.execute(
        "INSERT INTO dishes (name, price, available) VALUES (?, ?, ?)",
        [input.name, input.price, available]
      );
      const [rows] = await pool.execute(
        "SELECT id, name, price, available FROM dishes WHERE id = ?",
        [result.insertId]
      );
      const row = rows[0];
      return { ...row, available: Boolean(row.available), price: Number(row.price) };
    },
    updateDish: async (_, { id, input }) => {
      const allowedFields = ["name", "price", "available"];
      const fields = allowedFields.filter(field => input[field] !== undefined);
      if (fields.length === 0) {
        const [rows] = await pool.execute(
          "SELECT id, name, price, available FROM dishes WHERE id = ?",
          [id]
        );
        if (rows.length === 0) return null;
        const row = rows[0];
        return { ...row, available: Boolean(row.available), price: Number(row.price) };
      }

      const assignments = fields.map(field => `${field} = ?`).join(", ");
      const values = fields.map(field => input[field]);
      values.push(id);

      await pool.execute(
        `UPDATE dishes SET ${assignments} WHERE id = ?`,
        values
      );

      const [rows] = await pool.execute(
        "SELECT id, name, price, available FROM dishes WHERE id = ?",
        [id]
      );
      if (rows.length === 0) return null;
      const row = rows[0];
      return { ...row, available: Boolean(row.available), price: Number(row.price) };
    }
  }
};

async function start() {
  const app = express();
  app.use(cors());
  app.use(express.json());
  app.use(express.static(path.join(__dirname, "public")));

  const server = new ApolloServer({ typeDefs, resolvers });
  await server.start();

  app.use(
    "/graphql",
    expressMiddleware(server, {
      context: async () => ({ pool })
    })
  );

  const port = Number(process.env.PORT || 4000);
  app.listen(port, () => {
    console.log(`CampusEats is running at http://localhost:${port}`);
    console.log(`GraphQL endpoint: http://localhost:${port}/graphql`);
  });
}

start().catch(error => {
  console.error("Could not start the server:", error);
  process.exit(1);
});