
const express = require('express');

const app = express();
const PORT = 3000;

// Middleware
app.use(express.json());
app.use(express.static('public'));

// In-memory data
let items = [
    { id: 1, name: "Burger", price: 80, available: true },
    { id: 2, name: "Pizza", price: 150, available: true },
    { id: 3, name: "Sandwich", price: 60, available: false },
    { id: 4, name: "Pasta", price: 120, available: true }
];

let nextId = 5;

// GET: Display all menu items
app.get('/items', (req, res) => {
    res.status(200).json(items);
});

// GET: Display one item using ID
app.get('/items/:id', (req, res) => {

    const id = parseInt(req.params.id);

    const item = items.find(item => item.id === id);

    if (!item) {
        return res.status(404).json({
            error: "Item not found"
        });
    }

    res.status(200).json(item);
});

// POST: Add a new menu item
app.post('/items', (req, res) => {

    const { name, price, available } = req.body;

    // Validate required fields
    if (
        typeof name !== 'string' ||
        name.trim() === '' ||
        price === undefined ||
        price === null ||
        price === ''
    ) {
        return res.status(422).json({
            error: "Name and price are required"
        });
    }

    if (!Number.isFinite(Number(price)) || Number(price) < 0) {
        return res.status(422).json({
            error: "Price must be a valid non-negative number"
        });
    }

    const newItem = {
        id: nextId++,
        name: name.trim(),
        price: Number(price),
        available: available === undefined ? true : available
    };

    items.push(newItem);

    res.status(201).json(newItem);
});

// Start the server
app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
