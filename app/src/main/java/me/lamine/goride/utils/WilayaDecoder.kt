package me.lamine.goride.utils
var wilayaArrayEN = arrayOf(
    "Adrar",
    "Chlef",
    "Laghouat",
    "Oum el Bouaghi",
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
    "Tizi Ouzou",
    "Algiers",
    "Djelfa",
    "Jijel",
    "Sétif",
    "Saïda",
    "Skikda",
    "Sidi Bel Abbes",
    "Annaba",
    "Guelma",
    "Constantine",
    "Médéa",
    "Mostaganem",
    "M'Sila",
    "Mascara",
    "Ouargla",
    "Oran",
    "El Bayadh",
    "Illizi",
    "Bordj Bou Arreridj",
    "Boumerdès",
    "El Taref",
    "Tindouf",
    "Tissemsilt",
    "El Oued",
    "Khenchela",
    "Souk Ahras",
    "Mila",
    "Aïn Defla",
    "Naama",
    "Aïn Témouchent",
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
    "Bouira",
    "Tamanrasset",
    "Tébessa",
    "Tlemcen",
    "Tiaret",
    "Tizi Ouzou",
    "Alger",
    "Djelfa",
    "Jijel",
    "Sétif",
    "Saida",
    "Skikda",
    "Sidi Bel Abbès",
    "Annaba",
    "Guelma",
    "Constantine",
    "Médéa",
    "Mostaganem",
    "M'Sila",
    "Mascara",
    "Ouargla",
    "Oran",
    "El-Bayadh",
    "Illizi",
    "Bordj-Bou-Arréridj",
    "Boumerdès",
    "El-Taref",
    "Tindouf",
    "Tissemsilt",
    "ElOued",
    "Khenchela",
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
        if (wilaya.toLowerCase().contains(item.toLowerCase())) {
            return index + 1
        }
    }
    return decodeWilayaFR(wilaya)
}
private fun decodeWilayaFR(wilaya: String): Int {
    for ((index, item) in wilayaArrayFR.withIndex()) {
        if (wilaya.toLowerCase().contains(item.toLowerCase())) {
            return index + 1
        }
    }
    return 0
}