-dontwarn kotlinx.serialization.**

# OkHttp - Suppress warnings for Android, Conscrypt, and OpenJSSE specific classes
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn android.security.**
-dontwarn android.os.**
-dontwarn android.util.**
-dontwarn android.net.**
-dontwarn dalvik.system.**

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okio.** { *; }

# Notes related to dynamic access in OkHttp
-dontnote okhttp3.internal.platform.android.CloseGuard$Companion
-dontnote okhttp3.internal.platform.android.AndroidSocketAdapter

# Keep Semver4j library
-keep class com.vdurmont.semver4j.** { *; }
-keep interface com.vdurmont.semver4j.** { *; }
-dontnote com.vdurmont.semver4j.**

# Sun/Swing warnings
-dontwarn sun.misc.**
-dontwarn sun.swing.SwingUtilities2$AATextInfo
-dontwarn net.miginfocom.swing.MigLayout
-dontwarn sun.java2d.cmm.**


-dontwarn sun.misc.Unsafe
-dontwarn jdk.internal.misc.Unsafe
-keep class com.google.gson.internal.UnsafeAllocator { *; }
-keep class com.google.gson.internal.** { *; }

# Suppress notes
-dontnote kotlinx.serialization.**
-dontnote META-INF.**
-dontnote kotlinx.serialization.internal.PlatformKt

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep all serializable classes
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# Keep serializers
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}

# Keep serializable classes and their properties
-if @kotlinx.serialization.Serializable class **
-keep class <1> {
    static <1>$Companion Companion;
}

# Keep serialization classes
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.descriptors.** { *; }

# Keep data classes and their inner classes
-keep class AppSettings { *; }
-keep class WindowState { *; }
-keep class OpenFile { *; }
-keep class Database { *; }
-keep class MainViewModel { *; }
-keep class AppSettings$$serializer { *; }

# XML and DOM - Handle duplicate definitions
-dontnote javax.xml.**
-dontnote org.w3c.dom.**
-dontnote org.xml.sax.**
-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**

# Apache Commons
-dontwarn org.objectweb.asm.**
-dontwarn org.brotli.dec.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.tukaani.xz.**
-dontwarn org.apache.commons.compress.archivers.sevenz.**
-dontwarn org.apache.commons.compress.compressors.**
-dontwarn org.apache.commons.compress.harmony.**
-dontnote org.apache.commons.compress.**
-dontnote org.apache.commons.io.**
-dontnote org.apache.commons.lang3.**

# Apache Commons Logging
-keep class org.apache.commons.logging.impl.** { *; }
-keep class org.apache.commons.logging.LogFactory { *; }
-keep class org.apache.commons.logging.Log { *; }
-keepnames class org.apache.commons.logging.LogFactory
-keepnames class org.apache.commons.logging.impl.LogFactoryImpl
-keepnames class org.apache.commons.logging.impl.NoOpLog
-keepnames class org.apache.commons.logging.impl.SimpleLog
-dontwarn org.apache.commons.logging.**
-dontnote org.apache.commons.logging.**
-dontwarn javax.servlet.**
-dontwarn org.apache.avalon.framework.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.log.**
-dontnote org.apache.log4j.**
-dontnote org.apache.log.**

# SLF4J
-keep class org.slf4j.impl.** { *; }
-keep class org.slf4j.simple.** { *; }
-keepnames class org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.**
-dontnote org.slf4j.**
-keep class org.slf4j.LoggerFactory { *; }
-keep interface org.slf4j.Logger { *; }
-keep class org.slf4j.simple.SimpleServiceProvider implements org.slf4j.spi.SLF4JServiceProvider { *; }
-keepnames class * implements org.slf4j.spi.SLF4JServiceProvider
-keep interface org.slf4j.spi.SLF4JServiceProvider { *; }

# Apache PDFBox
-keep class org.apache.pdfbox.io.IOUtils {
    static final org.apache.commons.logging.Log LOG;
    *;
}
-keepclassmembers class org.apache.pdfbox.io.IOUtils {
    static final org.apache.commons.logging.Log LOG;
}

# Ensure Apache Commons Logging initialization works
-keep class org.apache.commons.logging.LogFactory {
    static org.apache.commons.logging.LogFactory factory;
    static java.util.Hashtable factories;
    *;
}
-keepclassmembers class org.apache.commons.logging.LogFactory {
    static *;
}
-dontnote org.apache.pdfbox.**
-dontnote org.apache.fontbox.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.pdfbox.io.IOUtils
-dontwarn org.apache.fontbox.**
-keep class org.apache.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }
-keep class org.apache.pdfbox.util.PDFBoxResourceLoader { *; }
-keepnames class * implements org.apache.pdfbox.pdmodel.graphics.image.ImageReader
-keepnames class * implements org.apache.pdfbox.pdmodel.font.FontProvider

# Deskit UI Library
-dontwarn com.github.zahid4kh.deskit.**
-dontnote com.github.zahid4kh.deskit.**
-keep public class com.github.zahid4kh.deskit.** {
    public protected *;
}
-keep public interface com.github.zahid4kh.deskit.** {
    public protected *;
}
-keepclassmembers enum com.github.zahid4kh.deskit.** {
    *;
}

-dontwarn com.sun.star.**
-dontwarn java.lang.ProcessBuilder

# Koin dependency injection
-dontwarn org.koin.**
-dontnote org.koin.**
-keep class org.koin.core.** { *; }
-keep class org.koin.core.scope.Scope { *; }
-keep class org.koin.core.parameter.ParametersHolder { *; }
-keep class org.koin.core.module.Module { *; }
-keep class org.koin.dsl.** { *; }

# Keep Koin related lambda functions in AppModule
-keepclassmembers class AppModuleKt {
    ** appModule$lambda$*(***);
}


# Keep only the necessary com.sun.star classes
-dontnote com.sun.star.**
-keep interface com.sun.star.** { *; }
-keep class com.sun.star.lib.uno.helper.UnoUrl { *; }
-keep class com.sun.star.comp.** { *; }
-keep class com.sun.star.lib.** { *; }
-keep class com.sun.star.uno.** { *; }

# Apache Batik and XMLGraphics
-dontwarn org.apache.batik.**
-dontwarn org.mozilla.javascript.**
-dontwarn org.python.**
-dontwarn org.apache.fop.**

# Suppress Batik and XMLGraphics dynamic loading notes
-dontnote org.apache.batik.**
-dontnote org.w3c.css.sac.helpers.ParserFactory
-dontnote javax.xml.datatype.DatatypeConfigurationException
-dontnote javax.xml.transform.TransformerException
-dontnote org.apache.xmlgraphics.ps.dsc.DSCCommentFactory
-dontnote org.apache.xmlgraphics.util.Service

# Keep Batik classes actually used
-keep class org.apache.batik.transcoder.** { *; }
-keep class org.apache.batik.transcoder.image.PNGTranscoder { *; }
-keep class org.apache.batik.bridge.** { *; }
-keep class org.apache.batik.dom.** { *; }
-keep class org.apache.batik.util.** { *; }
-keep class org.apache.batik.apps.** { *; }
-keep class org.apache.batik.ext.** { *; }
-keep class org.apache.batik.gvt.** { *; }
-keep class org.apache.xmlgraphics.** { *; }
-keep interface org.apache.batik.** { *; }

# Keep Batik service implementations
-keepnames class * implements org.apache.batik.ext.awt.image.spi.ImageWriter
-keepnames class * implements org.apache.batik.ext.awt.image.spi.ImageTranscoder
-keepnames class * implements org.apache.batik.gvt.font.FontFamily

# Fix for Batik's BridgeContext methods
-keepclassmembers class org.apache.batik.bridge.BatikWrapFactory {
    void setJavaPrimitiveWrap(boolean);
}
-keepclassmembers class org.apache.batik.bridge.EventTargetWrapper {
    java.lang.Object unwrap();
}
-keepclassmembers class org.apache.batik.bridge.GlobalWrapper {
    void defineFunctionProperties(java.lang.String[], java.lang.Class, int);
}
-keepclassmembers class org.apache.batik.bridge.WindowWrapper {
    void defineFunctionProperties(java.lang.String[], java.lang.Class, int);
    void defineProperty(java.lang.String, java.lang.Class, int);
}

# XML/SAX Support for Batik - Critical for SVG processing
-keep class javax.xml.** { *; }
-keep class org.xml.sax.** { *; }
-keep class org.xml.sax.ext.** { *; }
-keep class org.xml.sax.helpers.** { *; }
-keep class com.sun.org.apache.xerces.** { *; }
-keep class com.sun.org.apache.xalan.** { *; }

# Batik SAX/DOM classes - Essential for SVG transcoding
-keep class org.apache.batik.dom.** { *; }
-keep class org.apache.batik.anim.dom.** { *; }
-keep class org.apache.batik.parser.** { *; }
-keep class org.apache.batik.transcoder.** { *; }
-keep class org.apache.batik.transcoder.image.** { *; }

# XML Parser implementations
-keep class * implements org.xml.sax.XMLReader { *; }
-keep class * implements org.xml.sax.ext.LexicalHandler { *; }
-keep class * implements org.xml.sax.ContentHandler { *; }
-keep class * implements org.xml.sax.ErrorHandler { *; }

# Keep XML property setters/getters
-keepclassmembers class * {
    *** setProperty(...);
    *** getProperty(...);
    *** setFeature(...);
    *** getFeature(...);
}

# Suppress XML warnings but keep functionality
-dontwarn javax.xml.**
-dontwarn org.xml.sax.**
-dontwarn com.sun.org.apache.xerces.**
-dontwarn com.sun.org.apache.xalan.**

# Batik CSS Parser - Critical for SVG processing
-keep class org.apache.batik.css.parser.** { *; }
-keep class org.apache.batik.css.engine.** { *; }
-keep class org.apache.batik.css.** { *; }

# Batik Bridge classes that load CSS dynamically
-keep class org.apache.batik.bridge.** { *; }
-keep class org.apache.batik.gvt.** { *; }
-keep class org.apache.batik.script.** { *; }

# Keep all Batik parser implementations
-keep class * extends org.apache.batik.css.parser.AbstractParser { *; }
-keep class * implements org.apache.batik.css.parser.CSSParser { *; }

# Dynamic class loading for Batik
-keepnames class org.apache.batik.css.parser.Parser
-keepnames class org.apache.batik.css.engine.CSSEngine
-keepnames class org.apache.batik.dom.ExtensibleDOMImplementation

# Don't warn about optional dependencies
-dontwarn java.awt.**
-dontwarn javax.swing.**
# Gson support for JodConverter - More comprehensive
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keep class com.google.gson.internal.** { *; }
-keep class com.google.gson.reflect.** { *; }

# Critical for Gson to work with JodConverter DocumentFormat
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses


-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**
-dontnote com.google.gson.**


# Keep reflection access needed by your app
-keepclassmembers class * {
    @com.sun.star.lib.uno.typeinfo.TypeInfo <fields>;
}

# Coil3 Image Loading Library
-keep class coil3.** { *; }
-keep interface coil3.** { *; }
-dontwarn coil3.**
-dontnote coil3.**

# Keep Coil3 Service Loaders - Critical for network fetchers
-keep class coil3.util.** { *; }
-keep class coil3.network.** { *; }
-keep class coil3.network.okhttp.** { *; }
-keep class coil3.network.okhttp.internal.** { *; }

# Specifically keep the missing OkHttp network fetcher
-keep class coil3.network.okhttp.internal.OkHttpNetworkFetcherServiceLoaderTarget { *; }
-keep class coil3.network.okhttp.internal.OkHttpNetworkFetcher { *; }

# Keep all Coil3 service loader targets
-keep class * implements coil3.util.FetcherServiceLoaderTarget { *; }
-keep class * implements coil3.util.DecoderServiceLoaderTarget { *; }
-keep class * implements coil3.util.ServiceLoaderTarget { *; }

# Preserve service loader configuration files
-keepclassmembers class * {
    @coil3.annotation.ExperimentalCoilApi *;
}

# Keep META-INF service files that service loaders read
-keepclasseswithmembers class * {
    public static ** INSTANCE;
}

# Additional OkHttp classes that Coil3 might need
-keep class okhttp3.internal.** { *; }
-keep class okio.internal.** { *; }

# Service loader mechanism preservation
-keep class * extends java.util.ServiceLoader { *; }
-keep class * implements java.util.ServiceLoader$Provider { *; }

# Keep service loading related annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# Keep Semver4j library
-keep class com.vdurmont.semver4j.** { *; }
-keep interface com.vdurmont.semver4j.** { *; }
-dontnote com.vdurmont.semver4j.**