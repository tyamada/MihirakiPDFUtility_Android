package com.takumayamada22.pdfutility.data

import java.io.File

data class SamplePdfItem(
    val id: String,
    val title: String,
    val fileName: String,
    val url: String,
    val fileSize: String,
    val languageCode: String,
    val languageDisplayName: String
)

data class SamplePdfItemState(
    val item: SamplePdfItem,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val localFile: File? = null
)

object SamplePdfCatalog {
    private const val BASE_URL = "https://tyamada.github.io/MihirakiPDFViewer_Android/sample/"

    val items = listOf(
        SamplePdfItem(
            id = "ep1_en",
            title = "THE TRY-IT CLUB EPISODE 1 THE BREAK-TIME MAP",
            fileName = "tameshibu_episode1_2_en.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_en.pdf",
            fileSize = "23MB",
            languageCode = "en",
            languageDisplayName = "English"
        ),
        SamplePdfItem(
            id = "ep2_en",
            title = "THE TRY-IT CLUB EPISODE 2 ROOM TO GROW",
            fileName = "tameshibu_episode2_2_en.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_en.pdf",
            fileSize = "24MB",
            languageCode = "en",
            languageDisplayName = "English"
        ),
        SamplePdfItem(
            id = "ep1_ja",
            title = "ためし部 第１話 ひと息マップ",
            fileName = "tameshibu_episode1_2_ja.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_ja.pdf",
            fileSize = "23MB",
            languageCode = "ja",
            languageDisplayName = "Japanese"
        ),
        SamplePdfItem(
            id = "ep2_ja",
            title = "ためし部 第２話 机、ひろがる。",
            fileName = "tameshibu_episode2_2_ja.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_ja.pdf",
            fileSize = "24MB",
            languageCode = "ja",
            languageDisplayName = "Japanese"
        ),
        SamplePdfItem(
            id = "ep1_ko",
            title = "해봄부 제1화 한숨 돌림 지도",
            fileName = "tameshibu_episode1_2_ko.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_ko.pdf",
            fileSize = "23MB",
            languageCode = "ko",
            languageDisplayName = "Korean"
        ),
        SamplePdfItem(
            id = "ep2_ko",
            title = "해봄부제2화 책상이 넓어지다",
            fileName = "tameshibu_episode2_2_ko.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_ko.pdf",
            fileSize = "24MB",
            languageCode = "ko",
            languageDisplayName = "Korean"
        ),
        SamplePdfItem(
            id = "ep1_zh_cn",
            title = "试试社 第1话 歇口气地图",
            fileName = "tameshibu_episode1_2_zh_cn.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_zh_cn.pdf",
            fileSize = "23MB",
            languageCode = "zh_cn",
            languageDisplayName = "Chinese (Simplified)"
        ),
        SamplePdfItem(
            id = "ep2_zh_cn",
            title = "试试社 第2话 桌子变大了",
            fileName = "tameshibu_episode2_2_zh_cn.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_zh_cn.pdf",
            fileSize = "24MB",
            languageCode = "zh_cn",
            languageDisplayName = "Chinese (Simplified)"
        ),
        SamplePdfItem(
            id = "ep1_de",
            title = "DER PROBIERCLUB FOLGE 1",
            fileName = "tameshibu_episode1_2_de.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_de.pdf",
            fileSize = "23MB",
            languageCode = "de",
            languageDisplayName = "German"
        ),
        SamplePdfItem(
            id = "ep2_de",
            title = "DER PROBIERCLUB FOLGE 2",
            fileName = "tameshibu_episode2_2_de.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_de.pdf",
            fileSize = "24MB",
            languageCode = "de",
            languageDisplayName = "German"
        ),
        SamplePdfItem(
            id = "ep1_fr",
            title = "LE CLUB DES ESSAIS EPISODE 1",
            fileName = "tameshibu_episode1_2_fr.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_fr.pdf",
            fileSize = "23MB",
            languageCode = "fr",
            languageDisplayName = "French"
        ),
        SamplePdfItem(
            id = "ep2_fr",
            title = "LE CLUB DES ESSAIS EPISODE 2",
            fileName = "tameshibu_episode2_2_fr.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_fr.pdf",
            fileSize = "24MB",
            languageCode = "fr",
            languageDisplayName = "French"
        ),
        SamplePdfItem(
            id = "ep1_zh_tw",
            title = "試試社 第1話",
            fileName = "tameshibu_episode1_2_zh_tw.pdf",
            url = "${BASE_URL}tameshibu_episode1_2_zh_tw.pdf",
            fileSize = "23MB",
            languageCode = "zh_tw",
            languageDisplayName = "Chinese (Traditional)"
        ),
        SamplePdfItem(
            id = "ep2_zh_tw",
            title = "試試社 第2話",
            fileName = "tameshibu_episode2_2_zh_tw.pdf",
            url = "${BASE_URL}tameshibu_episode2_2_zh_tw.pdf",
            fileSize = "24MB",
            languageCode = "zh_tw",
            languageDisplayName = "Chinese (Traditional)"
        )
    )

    fun getAvailableLanguages(): List<String> {
        return items.map { it.languageCode }.distinct()
    }
}
