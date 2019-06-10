const http = require('http');
const server = http.createServer();

server.on('request', (req, res) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
  res.end(`<?xml version="1.0" encoding="utf-8"?><response code="999" label="Du har blivit utloggad på grund av inaktivitet." label_en-GB="You have been logged off due to inactivity." label_nl-NL="U bent uitgelogd wegens inactiviteit." label_nb-NO="Du har blitt logget av på grunn av inaktivitet."/>`);
});

server.listen(3002);
console.log(`Listening on localhost:3002`);
