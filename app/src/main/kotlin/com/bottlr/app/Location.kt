package com.bottlr.app

class Location {
    var timeDateAdded: String? = null
    var gpsCoordinates: String? = null
    var name: String? = null

    constructor(timeDateAdded: String, gpsCoordinates: String, name: String) {
        this.timeDateAdded = timeDateAdded
        this.gpsCoordinates = gpsCoordinates
        this.name = name
    }

}