var fs = require('fs');
var https = require('https');
var options = {
  //key: fs.readFileSync(<ficheiro PEM com chave privada do servidor>), 
  key: fs.readFileSync('key.pem'),

  //cert: fs.readFileSync(<ficheiro PEM com certificado do servidor>), 
  cert: fs.readFileSync('cert.pem'),

  //ca: fs.readFileSync(<ficheiro PEM com certificado da CA root>), 
  ca: fs.readFileSync('CA1.pem'),
  requestCert: true,
  rejectUnauthorized: true
};

https.createServer(options, function (req, res) {
  console.log(new Date() + ' ' +
    req.connection.remoteAddress + ' ' +
    req.socket.getPeerCertificate().subject.CN + ' ' +
    req.method + ' ' + req.url);
  res.writeHead(200);
  res.end("Secure Hello World with node.js\n");
}).listen(4433);

console.log('Listening @4433');