package com.altimedia.libplatform;

import android.os.RecoverySystem;

import java.io.File;
import java.io.IOException;

public class HiddenApi {
    public static boolean verifyPackageCompatibility(File file) throws IOException {
     return RecoverySystem.verifyPackageCompatibility(file);
   }
}
