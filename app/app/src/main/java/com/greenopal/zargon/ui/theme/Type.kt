package com.greenopal.zargon.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.greenopal.zargon.R
import androidx.compose.material3.Typography

val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val CinzelFamily = FontFamily(
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Cinzel"), fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
)

val CinzelDecorativeFamily = FontFamily(
    Font(googleFont = GoogleFont("Cinzel Decorative"), fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Cinzel Decorative"), fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Cinzel Decorative"), fontProvider = GoogleFontsProvider, weight = FontWeight.Black),
)

val IMFellFamily = FontFamily(
    Font(googleFont = GoogleFont("IM Fell English"), fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("IM Fell English"), fontProvider = GoogleFontsProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
)

val Typography = Typography(

    displayLarge = TextStyle(
        fontFamily   = CinzelDecorativeFamily,
        fontWeight   = FontWeight.Black,
        fontSize     = 32.sp,
        lineHeight   = 38.sp,
        letterSpacing = 0.04.sp,
    ),

    headlineLarge = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 24.sp,
        lineHeight   = 30.sp,
        letterSpacing = 0.08.sp,
    ),

    headlineMedium = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 20.sp,
        lineHeight   = 26.sp,
        letterSpacing = 0.05.sp,
    ),

    headlineSmall = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.12.sp,
    ),

    titleMedium = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 15.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.05.sp,
    ),

    titleSmall = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.06.sp,
    ),

    bodyLarge = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily   = IMFellFamily,
        fontWeight   = FontWeight.Normal,
        fontStyle    = FontStyle.Italic,
        fontSize     = 14.sp,
        lineHeight   = 22.sp,
    ),

    bodySmall = TextStyle(
        fontFamily   = IMFellFamily,
        fontWeight   = FontWeight.Normal,
        fontStyle    = FontStyle.Italic,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
    ),

    labelSmall = TextStyle(
        fontFamily   = CinzelFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 10.sp,
        lineHeight   = 14.sp,
        letterSpacing = 0.10.sp,
    ),
)
