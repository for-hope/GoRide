package me.lamine.goride


import java.util.Arrays


class User {
    var carMake: String? = null
        private set
    var carModel: String? = null
        private set
    var isFemale: Boolean = false

    constructor(fullname: String, username: String) {
        this.carMake = fullname
        this.carModel = username
    }

    constructor(fullname: String, username: String, female: Boolean) {
        this.carMake = fullname
        this.carModel = username
        this.isFemale = female
    }

    companion object {

        var USERS = Arrays.asList(

            User("Lori Rice", "lori__rice", true),
            User("Karen Sandoval", "karen93", true),
            User("Andrea Wagner", "andrea_86"),
            User("Jerry Sanchez", "jerry-85"),
            User("Elizabeth Carroll", "elizabeth-94", true),
            User("Ronald Tran", "ronald_tran"),
            User("Crystal Castillo", "crystal.castillo", true),
            User("Sean King", "sean"),
            User("Paul Aguilar", "paul.aguilar"),
            User("Benjamin Gonzalez", "ben-85"),
            User("Ryan Curtis", "ryan-94"),
            User("Jane Willis", "jane_willis", true),
            User("Diane Price", "diane__price", true),
            User("Marie Elliott", "marie95", true),
            User("Peter Cole", "peter_83"),
            User("Donald Green", "donald-35"),
            User("Frank Oliver", "frank-oliver"),
            User("Doris Walters", "doris", true),
            User("Jack Lynch", "jack-lynch"),
            User("Ruth Patel", "patel"),
            User("Donald Obrien", "obrien.donald"),
            User("Joyce Wells", "jwells"),
            User("Austin Keller", "keller-94"),
            User("Jean Watkins", "jw", true),
            User("Julio Cesar Paredes", "julio.cesar"),
            User("Fabian Mercedesz", "fabian"),
            User("Roma Kania", "roma.kania", true),
            User("Luna Vidal", "luna-75", true),
            User("Daisy Roberts", "roberts-93", true),
            User("Matthew Maxton", "matthew_maxton"),
            User("Claudio Guerra", "guerra.claudio"),
            User("Floare Carafoli", "floare84", true),
            User("Esra Yilmaz", "esra_83", true),
            User("Casanda Goian", "casanda-1935", true),
            User("Kyle Lawson", "kyle-law"),
            User("Mathijs de Boer", "mdboer"),
            User("Mitchell Sarah", "mitchell-sarah"),
            User("Carolina Rotaru", "rotaru", true),
            User("Joe Fernandez", "joe.fernandez"),
            User("Christian Colombo", "ccolombo"),
            User("Venera Steflea", "venera-91", true),
            User("Helge Olsen", "holsen", true),
            User("Fien Smet", "fien.smet"),
            User("Hugo Aviles", "aviles"),
            User("Elizabeth Montoya", "elizabeth.montoya", true),
            User("Mihnea Gliga", "mihnea-75", true),
            User("Gary Cook", "cook-96"),
            User("Seppe Smet", "seppe_smet"),
            User("Diane Lane", "diane.lane", true),
            User("Sophia Ackroyd", "sophia", true),
            User("Octavia Sirma", "octavia_sirma", true),
            User("Ciprian Tutoveanu", "ciprian"),
            User("Ida Birkeland", "birkeland-ida", true),
            User("Tore Haugland", "torehaug"),
            User("Denis Vaska", "denis-vaska"),
            User("Milena Corbea", "corbeamilena", true),
            User("Gyurkovics Letti", "gyur.letti "),
            User("Oliviu Fugaru", "oliviufu"),
            User("Semiha Erdem", "semi-91"),
            User("Codin Ardelean", "codin.ardelean")
        )
    }
}