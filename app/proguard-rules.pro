


-dontskipnonpubliclibraryclasses


-keepnames class org.apache.** {*;}
-keep public class org.apache.** {*;}
-keep class android.support.v4.app.** { *; }
-keep class android.support.v4.view.ViewPager{ *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.widget.RecyclerView{*;}
-keep class android.support.v7.widget.RecyclerView$**{*;}
-keep class android.support.v7.widget.RecyclerView$**{<methods>;}
-keep class android.support.v7.widget.GridLayoutManager{*;}
-keep class android.support.v7.widget.GridLayoutManager$**{*;}
-keep class android.support.v7.widget.LinearLayoutManager{*;}
-keep class android.support.v7.widget.LinearLayoutManager$**{*;}
-keep class android.support.v7.widget.Toolbar{*;}
-keep class com.dineout.book.model.webservice.Solr.**{*;}
-keep class com.dineout.book.model.webservice.**{*;}
-keep class com.dineout.book.adapter.**{*;}
-keep class com.paytm.pgsdk.** {*;}
-keep class  com.mixpanel.android.** {*;}
-keep interface com.paytm.pgsdk.** {*;}
-keep class com.til.colombia.dmp.android.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class in.til.sdk.** { *; }
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**


-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

-keep class com.dineout.android.volley.**{*;}
-keep class com.example.dineoutnetworkmodule.*
-keep class com.google.android.gms.**{*;}
-keep interface com.google.android.gms.**{*;}
-keep class com.mixpanel.android.mpmetrics.**{*;}
-keep interface com.mixpanel.android.mpmetrics.**{*;}

-keep public class * extends java.lang.Exception
-keepattributes SourceFile,LineNumberTable

-keep class com.freshdesk.hotline.** { *; }
#-keep class com.clevertap.android.*
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient
-keep class com.apsalar.sdk.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}


-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn com.dineout.android.volley.**
-dontwarn com.google.android.gms.**
-dontwarn com.mixpanel.android.mpmetrics.**

-dontwarn com.demach.konotor.**
#-dontwarn com.clevertap.android.**
-keep class android.support.v4.app.Fragment { *; }
-keep class android.support.v4.view.ViewPager
-keep class android.support.v4.**
-keepclassmembers class android.support.v4.view.ViewPager$LayoutParams {*;}
-keep class com.dineout.book.widgets.** { *; }


-keep class com.dineout.android.volley.** { *; }
-keep class android.support.v4.** { *; }
-keep class com.github.nkzawa.**

-keep class com.mobikwik.sdk.**{*;}
-keep class com.google.android.gms.ads.identifier.** { *; }

-dontwarn com.mobikwik.sdk.**
-keep class com.payu.payuui.**{*;}
-dontwarn com.payu.payuui.**
-keep class com.payu.magicretry.**{*;}
-keepclassmembers class com.payu.custombrowser.** {
      *;
      }
-keepclassmembers class * {
@android.webkit.JavascriptInterface <methods>;
 }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes JavascriptInterface

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Classes that will be serialized/deserialized over Gson
-keep class com.optimizely.JSON.** { *; }
-dontwarn org.apache.**
-dontwarn  org.w3c.dom.bootstrap.**
-dontwarn  org.ietf.jgss.**
-dontwarn com.freshdesk.hotline.**

-dontwarn okio.**

-keepattributes EnclosingMethod

-printmapping build/outputs/mapping/release/mapping.txt

# otto bus
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# keep countly
-keep class ly.count.android.sdk.**{ *; }
-keep class org.openudid.**{ *; }

-ignorewarnings