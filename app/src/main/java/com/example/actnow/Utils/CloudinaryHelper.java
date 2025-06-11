package com.example.actnow.Utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    
    public interface UploadCallback {
        void onComplete(String url, String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        String extension = getMimeType(context, imageUri);
        if (extension == null) {
            callback.onComplete(null, "Неподдерживаемый тип файла");
            return;
        }

        Map<String, Object> options = new HashMap<>();
        options.put("folder", "profile_images");
        options.put("resource_type", "image");

        MediaManager.get()
            .upload(imageUri)
            .options(options)
            .callback(new com.cloudinary.android.callback.UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Upload started
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    // Upload progress
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String url = (String) resultData.get("secure_url");
                    callback.onComplete(url, null);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    callback.onComplete(null, error.getDescription());
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    callback.onComplete(null, error.getDescription());
                }
            })
            .dispatch();
    }

    private static String getMimeType(Context context, Uri uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            extension = extension.toLowerCase();
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return null;
    }

    public static void deleteImage(String publicId, UploadCallback callback) {
        // Implement image deletion if needed
        MediaManager.get().cancelRequest(publicId);
    }
} 