const express = require("express");
const app = express();

app.get("/test", (req, res) => {
    res.status(200).json({success: true });
});

app.listen(1337, () => console.log("Server is running on port 1337"));