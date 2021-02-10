const sqlite3 = require('sqlite3').verbose();

let db = new sqlite3.Database('Datenbank');

app.get('/:produce', (req, res) => {
    let sql= `SELECT ALL Name ` +req.params.produce+`FROM Produce`
    let result
    db.all(sql, [], (err, rows) => {
        if (err) {
          throw err;
        }
        rows.forEach((row) => {
          //console.log(row.name);
          //wird nix gesendet möglicherweise so?
          result+=row
          
        })
      })
      res.send(result)

})

app.get('/mittelpunkt/:laendercode', (req, res) => {
    let sql= `SELECT ALL Name ` +parseString(req.params.laendercode)+`FROM origin/Laendercode`
    
    db.all(sql, [], (err, rows) => {
        if (err) {
          throw err;
        }
        rows.forEach((row) => {
          //console.log(row.name);
        });
     }) ;

})

app.get('/geojson/:laendercode', (req, res) => {
    let sql= `SELECT ALL Name ` +parseString(req.params.laendercode)+`FROM geojson/Laendercode`
    
    db.all(sql, [], (err, rows) => {
        if (err) {
          throw err;
        }
        rows.forEach((row) => {
          //console.log(row.name);
        });
      });

})

// close the database connection
db.close((err) => {
    if (err) {
      return console.error(err.message);
    }
    console.log('Close the database connection.');
  });