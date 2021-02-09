const knex = require("./knex.js")
const sqlite3 = require('sqlite3').verbose()

function getMittelpunkt(laendercode) {
    return knex("mittelpunkt").where("laendercode", laendercode).select();
}

function getGeojson(laendercode) {
    //return knex("geo_daten").where("laendercode", laendercode).select();
    knex("geo_daten").where("laendercode", laendercode).select().then(data=>{
        //console.log(data[0].geo_json)

         //extrahiert das JSON objekt aus dem string    
        let obj= JSON.parse(data[0].geo_json)
       

    //ausgabe zur kontrolle
        console.log(data)
        //console.log(data.geo_daten)

        //ersetzt den string durch das objekt
        data.geo_daten=obj

        //gibt datenobjekt zurück (momentan leer)
        return data
    })
}

function getProduce(name) {
    return knex("produce").where("name", name).select();
}

module.exports = {
    getMittelpunkt,
    getGeojson,
    getProduce
};