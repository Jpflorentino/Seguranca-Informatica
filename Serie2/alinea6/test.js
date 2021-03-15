var fs = require('fs')
var data = fs.readFileSync("credentials.json", 'utf8')
console.log(JSON.parse(data).web.client_id)

//----------------------------------------------------------
const DB = [{
    id: 1
}]

function verifyId(id) {
    return DB.some(element => element.id === id)
}
console.log(verifyId(1))

//----------------------------------------------------------