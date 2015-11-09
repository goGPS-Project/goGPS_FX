package org.gogpsproject.fx.storage;

import java.util.prefs.Preferences;

import net.java.html.js.JavaScriptBody;

public class StorageManager {

  private static Storage LOCAL = new Storage() {

    @Override
    public void put(String key, String val) {
      StorageManager.put_impl(key, val);
    }

    @Override
    public String get(String key) {
      return StorageManager.get_impl(key);
    }

  };

  private static Storage FALLBACK = new Storage() {

    @Override
    public void put(String key, String val) {
      Preferences pref = Preferences.userNodeForPackage(StorageManager.class);
      pref.put(key, val);
    }

    @Override
    public String get(String key) {
      Preferences pref = Preferences.userNodeForPackage(StorageManager.class);
      return pref.get(key, "");
    }
  };

  private static Storage EMPTY = new Storage() {

    @Override
    public void put(String key, String val) {

    }

    @Override
    public String get(String key) {
      return "";
    }
  };

  public interface Storage {

    public void put(String key, String val);

    public String get(String key);
  }

  public static Storage getStorage() {
    if (supportsLocalStorage()) {
      return LOCAL;
    } else if (supportsPreferences()) {
      return FALLBACK;
    }
    return EMPTY;
  }

  @JavaScriptBody(args = {}, body = "try {\n"
      + "    return 'localStorage' in window && window['localStorage'] !== null;\n"
      + "  } catch (e) {\n" + "    return false;\n" + "  }")
  private static native boolean supportsLocalStorage();

  private static boolean supportsPreferences() {
    try {
      Preferences userNodeForPackage = Preferences
          .userNodeForPackage(StorageManager.class);
      userNodeForPackage
          .put("dummy_probably_very_unlikely_s0mbody_U5e5_th1s_key", "-1");

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @JavaScriptBody(args = { "key" }, body = "if(localStorage){"
      + "  var obj = localStorage[key]; " + "  return obj;" + "}"
      + "return '';")
  public static native String get_impl(String key);

  @JavaScriptBody(args = { "key",
      "val" }, body = "var store = window['localStorage'];if (store) localStorage[key]=val;")
  public static native void put_impl(String key, String val);

}
