const express = require('express');
const app = express();
const sqlite3 = require('sqlite3').verbose();


let db = new sqlite3.Database('Datenbank');

app.get("/produce/:name", async (req, res) => {
  const produce = await db.getProduce (req.params.name);
  res.status(200).json({ produce });

});

app.get("/mittelpunkt/:laendercode", async (req, res) => {
  const mittelpunkt = await db.getMittelpunkt (req.params.laendercode);
  res.status(200).json({ mittelpunkt });
});

app.get("/geo_daten/:laendercode", async (req, res)=>{
  const geo_daten = await db.getGeojson (req.params.laendercode);
  res.status(200).json({ geo_daten }); 
});    
  
  /*let sql= `SELECT ALL Name ` +parseString(req.params.produce)+`FROM produce`
    
    db.all(sql, [], (err, rows) => {
        if (err) {
          throw err;
        }
        rows.forEach((row) => {
          //console.log(row.name);
        });
      });

})

app.get('/mittelpunkt/:laendercode', (req, res) => {
    let sql= `SELECT ALL Name ` +(req.params.produce)+`FROM mittelpunkt`
    
    db.all(sql, [], (err, rows) => {
        if (err) {
          throw err;
        }
        rows.forEach((row) => {
          //console.log(row.name);
        });
     }) ;

})

app.get('/:geojson/Laendercode', (req, res) => {
    let sql= `SELECT ALL Name ` +parseString(req.params.produce)+`FROM geo_daten`
    
    db.all(sql, [], (err, rows) => {
        if (err) {
          throw err;
        }
        rows.forEach((row) => {
          //console.log(row.name);
        });
      });

})*/

// close the database connection
db.close((err) => {
    if (err) {
      return console.error(err.message);
    }
    console.log('Close the database connection.');
  });

  app.listen(1337, () => console.log("Server is running on port 1337"));