package me.lamine.goride.utils

var wilayaArrayEN = arrayOf(
    "Adrar",
    "Chlef",
    "Laghouat",
    "Oumel-Bouaghi",
    "Batna",
    "Béjaïa",
    "Biskra",
    "Béchar",
    "Blida",
    "Bouira",
    "Tamanghasset",
    "Tebessa",
    "Tlemcen",
    "Tiaret",
    "TiziOuzou",
    "Algiers",
    "Djelfa",
    "Jijel",
    "Sétif",
    "Saïda",
    "Skikda",
    "SidiBelAbbes",
    "Annaba",
    "Guelma",
    "Constantine",
    "Médéa",
    "Mostaganem",
    "M'Sila",
    "Mascara",
    "Ouargla",
    "Oran",
    "ElBayadh",
    "Illizi",
    "BordjBouArréridj",
    "Boumerdès",
    "ElTaref",
    "Tindouf",
    "Tissemsilt",
    "ElOued",
    "Khenchela",
    "SoukAhras",
    "Mila",
    "AïnDefla",
    "Naama",
    "AïnTémouchent",
    "Ghardaïa",
    "Relizane"
)
var wilayaArrayFR = arrayOf(
    "Adrar",
    "Chlef",
    "Laghouat",
    "OumEl-Bouaghi",
    "Batna",
    "Béjaïa",
    "Biskra",
    "Béchar",
    "Blida",
    "0Bouira",
    "Tamanrasset",
    "Tébessa",
    "Tlemcen",
    "Tiaret",
    "TiziOuzou",
    "Alger",
    "Djelfa",
    "Jijel",
    "Sétif",
    "0Saida",
    "Skikda",
    "SidiBelAbbes",
    "Annaba",
    "Guelma",
    "Constantine",
    "Médéa",
    "Mostaganem",
    "M'Sila",
    "Mascara",
    "0Ouargla",
    "Oran",
    "El-Bayadh",
    "Illizi",
    "Bord-Bou-Arréridj",
    "Boumerdès",
    "El-Taref",
    "Tindouf",
    "Tissemsilt",
    "ElOued",
    "0Khenchela",
    "SoukAhras",
    "Tipaza",
    "Mila",
    "AïnDefla",
    "Naâma",
    "AïnTémouchent",
    "Ghardaïa",
    "Relizane"
)

fun decodeWilaya(wilaya: String): Int {
    for ((index, item) in wilayaArrayEN.withIndex()) {
        if (wilaya.contains(item)) {
            return index + 1
        }
    }
    return decodeWilayaFR(wilaya)
}

private fun decodeWilayaFR(wilaya: String): Int {
    for ((index, item) in wilayaArrayFR.withIndex()) {
        if (wilaya.contains(item)) {
            return index + 1
        }
    }
    return 0
}