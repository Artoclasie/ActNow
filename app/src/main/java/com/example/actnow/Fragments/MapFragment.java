package com.example.actnow.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.EventAdapter;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapFragment";
    private static final String MAPTILER_API_KEY = "dg0jVpNw8RFjDHYjqmLr";

    private WebView webView;
    private EditText etSearch;
    private Button btnSearch;
    private Button btnConfirmLocation;
    private FloatingActionButton fabSearch;
    private LinearLayout searchContainer;
    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList;
    private GeoPoint userLocation;
    private GeoPoint selectedLocation;
    private GeoPoint organizerLocation;
    private boolean isSelectMode = false;
    private GeoPoint currentEventLocation;
    private String organizerCity;
    private String lastSearchedAddress;

    // Границы РБ
    private static final double MIN_LAT = 51.26;
    private static final double MAX_LAT = 56.17;
    private static final double MIN_LNG = 23.17;
    private static final double MAX_LNG = 32.77;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventList = new ArrayList<>();
        if (getArguments() != null) {
            isSelectMode = getArguments().getBoolean("isSelectMode", false);
            if (getArguments().containsKey("lat") && getArguments().containsKey("lng")) {
                double lat = getArguments().getDouble("lat");
                double lng = getArguments().getDouble("lng");
                if (isWithinBelarusBounds(lat, lng)) {
                    selectedLocation = new GeoPoint(lat, lng);
                }
            }
            if (getArguments().containsKey("eventLat") && getArguments().containsKey("eventLng")) {
                double eventLat = getArguments().getDouble("eventLat");
                double eventLng = getArguments().getDouble("eventLng");
                if (isWithinBelarusBounds(eventLat, eventLng)) {
                    currentEventLocation = new GeoPoint(eventLat, eventLng);
                }
            }
            if (getArguments().containsKey("organizerCity")) {
                organizerCity = getArguments().getString("organizerCity");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        webView = view.findViewById(R.id.webview_map);
        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        btnConfirmLocation = view.findViewById(R.id.btn_confirm_location);
        fabSearch = view.findViewById(R.id.fab_search);
        searchContainer = view.findViewById(R.id.search_container);
        rvEvents = view.findViewById(R.id.rv_events);

        setupWebView();
        btnConfirmLocation.setVisibility(isSelectMode ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(isSelectMode ? View.GONE : View.VISIBLE);
        searchContainer.setVisibility(View.GONE);

        setupRecyclerView();
        setupListeners();

        return view;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true); // Разрешаем доступ к файлам из file:///
        webSettings.setAllowFileAccessFromFileURLs(true); // Разрешаем доступ к файлам из file:///
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // Разрешаем смешанный контент (http/https)

        // Включаем отладку WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + description + " URL: " + failingUrl);
                Toast.makeText(requireContext(), "Ошибка загрузки карты: " + description, Toast.LENGTH_LONG).show();
                view.loadData("<html><body><h1>Map failed to load</h1></body></html>", "text/html", "UTF-8");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page finished loading: " + url);
                webView.evaluateJavascript(
                        "window.onerror = function(message, source, lineno, colno, error) {" +
                                "    if (window.Android) {" +
                                "        window.Android.onMessage('JS Error: ' + message + ' at ' + source + ':' + lineno);" +
                                "    }" +
                                "    return true;" +
                                "};",
                        null
                );
                webView.evaluateJavascript(
                        "console.log = function(message) {" +
                                "    if (window.Android) {" +
                                "        window.Android.onMessage('JS Log: ' + message);" +
                                "    }" +
                                "};",
                        null
                );
                if (organizerCity != null && organizerLocation == null) {
                    new GeocodingTask(organizerCity, true).execute();
                } else if (isSelectMode && selectedLocation != null) {
                    moveToLocation(selectedLocation.getLatitude(), selectedLocation.getLongitude(), 18);
                } else if (organizerLocation != null) {
                    moveToLocation(organizerLocation.getLatitude(), organizerLocation.getLongitude(), 18);
                    addMarker(organizerLocation.getLatitude(), organizerLocation.getLongitude(), "Местоположение организатора", "", true, false);
                } else if (userLocation != null && isWithinBelarusBounds(userLocation.getLatitude(), userLocation.getLongitude())) {
                    moveToLocation(userLocation.getLatitude(), userLocation.getLongitude(), 18);
                }
                loadEvents();
            }
        });

        Log.d(TAG, "Attempting to load file:///android_asset/map/map.html");
        try {
            webView.loadUrl("file:///android_asset/map/map.html");
            Log.d(TAG, "Map.html loading initiated");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load map", e);
            Toast.makeText(requireContext(), "Файл map.html не найден: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext(), eventList, event -> {
            GeoPoint eventGeoPoint = event.getCoordinates();
            if (isWithinBelarusBounds(eventGeoPoint.getLatitude(), eventGeoPoint.getLongitude())) {
                moveToLocation(eventGeoPoint.getLatitude(), eventGeoPoint.getLongitude(), 18);
                if (userLocation != null) {
                    float distance = calculateDistance(userLocation, eventGeoPoint);
                    Toast.makeText(getContext(), "Расстояние: " + String.format("%.2f", distance) + " км", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Событие вне границ РБ", Toast.LENGTH_SHORT).show();
            }
        });
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEvents.setAdapter(eventAdapter);
    }

    private void setupListeners() {
        fabSearch.setOnClickListener(v -> {
            if (searchContainer.getVisibility() == View.VISIBLE) {
                searchContainer.setVisibility(View.GONE);
                fabSearch.setImageResource(android.R.drawable.ic_menu_search);
            } else {
                searchContainer.setVisibility(View.VISIBLE);
                fabSearch.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                etSearch.requestFocus();
            }
        });

        btnSearch.setOnClickListener(v -> {
            String address = etSearch.getText().toString().trim();
            if (!address.isEmpty()) {
                lastSearchedAddress = address;
                loadEventsByCity(parseCity(address));
                new GeocodingTask(address, false).execute();
            } else {
                Toast.makeText(getContext(), "Введите адрес", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.getLatitude());
                resultIntent.putExtra("longitude", selectedLocation.getLongitude());
                resultIntent.putExtra("address", lastSearchedAddress != null ? lastSearchedAddress : etSearch.getText().toString());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, resultIntent);
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Выберите местоположение на карте", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseCity(String address) {
        String[] parts = address.split(",");
        return parts.length > 0 ? parts[0].trim() : address;
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        android.location.LocationManager locationManager = (android.location.LocationManager) requireContext().getSystemService(requireContext().LOCATION_SERVICE);
        android.location.Location lastLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            double lat = lastLocation.getLatitude();
            double lng = lastLocation.getLongitude();
            if (isWithinBelarusBounds(lat, lng)) {
                userLocation = new GeoPoint(lat, lng);
                if (organizerLocation == null) {
                    addMarker(lat, lng, "Ваше местоположение", "", true, false);
                    moveToLocation(lat, lng, 18);
                }
                sortEventsByDistance();
            } else {
                Toast.makeText(getContext(), "Ваше местоположение вне РБ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        }
    }

    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    eventList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        EventModel event = document.toObject(EventModel.class);
                        event.setEventId(document.getId());
                        if (event.getCoordinates() != null && isWithinBelarusBounds(event.getCoordinates().getLatitude(), event.getCoordinates().getLongitude())) {
                            eventList.add(event);
                            Log.d(TAG, "Event added: " + event.getTitle() + " at " + event.getCoordinates().getLatitude() + ", " + event.getCoordinates().getLongitude());
                        } else {
                            Log.d(TAG, "Event skipped (out of bounds or no coordinates): " + event.getTitle());
                        }
                    }
                    Log.d(TAG, "Total events loaded: " + eventList.size());
                    sortEventsByDistance();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Ошибка загрузки мероприятий: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to load events", e);
                    }
                });
    }

    private void loadEventsByCity(String city) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("city", city)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    eventList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        EventModel event = document.toObject(EventModel.class);
                        event.setEventId(document.getId());
                        if (event.getCoordinates() != null && isWithinBelarusBounds(event.getCoordinates().getLatitude(), event.getCoordinates().getLongitude())) {
                            eventList.add(event);
                            Log.d(TAG, "Event added: " + event.getTitle() + " at " + event.getCoordinates().getLatitude() + ", " + event.getCoordinates().getLongitude());
                        } else {
                            Log.d(TAG, "Event skipped (out of bounds or no coordinates): " + event.getTitle());
                        }
                    }
                    Log.d(TAG, "Total events loaded: " + eventList.size());
                    sortEventsByDistance();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Ошибка загрузки мероприятий: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to load events", e);
                    }
                });
    }

    private void sortEventsByDistance() {
        if (userLocation == null || getContext() == null) return;

        Collections.sort(eventList, (e1, e2) -> {
            float distance1 = calculateDistance(userLocation, e1.getCoordinates());
            float distance2 = calculateDistance(userLocation, e2.getCoordinates());
            return Float.compare(distance1, distance2);
        });

        updateMapAndList();
    }

    private void updateMapAndList() {
        if (getContext() == null) return;
        clearMarkers();
        for (EventModel event : eventList) {
            GeoPoint eventGeoPoint = event.getCoordinates();
            float distance = userLocation != null ? calculateDistance(userLocation, eventGeoPoint) : 0;
            boolean isEventMarker = currentEventLocation != null && eventGeoPoint.getLatitude() == currentEventLocation.getLatitude() && eventGeoPoint.getLongitude() == currentEventLocation.getLongitude();
            addMarker(eventGeoPoint.getLatitude(), eventGeoPoint.getLongitude(),
                    event.getTitle(), "Расстояние: " + String.format("%.2f", distance) + " км",
                    false, isEventMarker);
        }
        eventAdapter.notifyDataSetChanged();
        if (!eventList.isEmpty()) {
            GeoPoint firstEvent = eventList.get(0).getCoordinates();
            moveToLocation(firstEvent.getLatitude(), firstEvent.getLongitude(), 18);
        }
    }

    private float calculateDistance(GeoPoint point1, GeoPoint point2) {
        float[] results = new float[1];
        Location.distanceBetween(point1.getLatitude(), point1.getLongitude(), point2.getLatitude(), point2.getLongitude(), results);
        return results[0] / 1000;
    }

    private boolean isWithinBelarusBounds(double lat, double lng) {
        return lat >= MIN_LAT && lat <= MAX_LAT && lng >= MIN_LNG && lng <= MAX_LNG;
    }

    private void addMarker(double lat, double lng, String title, String snippet, boolean isUserMarker, boolean isEventMarker) {
        if (isWithinBelarusBounds(lat, lng)) {
            webView.evaluateJavascript("javascript:window.postMessage(" +
                    "{" +
                    "  'action': 'addMarker'," +
                    "  'lat': " + lat + "," +
                    "  'lng': " + lng + "," +
                    "  'title': '" + title.replace("'", "\\'") + "'," +
                    "  'snippet': '" + snippet.replace("'", "\\'") + "'," +
                    "  'isUserMarker': " + isUserMarker + "," +
                    "  'isEventMarker': " + isEventMarker +
                    "}, '*')", null);
        }
    }

    private void clearMarkers() {
        webView.evaluateJavascript("javascript:window.postMessage({'action': 'clearMarkers'}, '*')", null);
    }

    private void moveToLocation(double lat, double lng, int zoom) {
        if (isWithinBelarusBounds(lat, lng)) {
            webView.evaluateJavascript("javascript:window.postMessage({" +
                    "'action': 'moveToLocation'," +
                    "'lat': " + lat + "," +
                    "'lng': " + lng + "," +
                    "'zoom': " + zoom +
                    "}, '*')", null);
        }
    }

    private class GeocodingTask extends AsyncTask<String, Void, GeoPoint> {
        private final String address;
        private final boolean isOrganizer;

        GeocodingTask(String address, boolean isOrganizer) {
            this.address = address;
            this.isOrganizer = isOrganizer;
        }

        @Override
        protected GeoPoint doInBackground(String... params) {
            try {
                String query = URLEncoder.encode(address + ", Belarus", "UTF-8");
                String urlString = "https://api.maptiler.com/geocoding/" + query + ".json?key=" + MAPTILER_API_KEY + "&language=ru";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "ActNowApp/1.0");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.getJSONArray("features").length() > 0) {
                    JSONObject feature = jsonObject.getJSONArray("features").getJSONObject(0);
                    JSONObject geometry = feature.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double lng = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);
                    if (isWithinBelarusBounds(lat, lng)) {
                        return new GeoPoint(lat, lng);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoding error: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GeoPoint result) {
            if (result != null) {
                if (isOrganizer) {
                    organizerLocation = result;
                    moveToLocation(result.getLatitude(), result.getLongitude(), 18);
                    addMarker(result.getLatitude(), result.getLongitude(), "Местоположение организатора", "", true, false);
                } else {
                    selectedLocation = result;
                    clearMarkers();
                    moveToLocation(result.getLatitude(), result.getLongitude(), 18);
                    addMarker(result.getLatitude(), result.getLongitude(), "Выбранное местоположение", lastSearchedAddress, false, false);
                }
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Адрес не найден в пределах РБ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onMapClick(double lat, double lng) {
            requireActivity().runOnUiThread(() -> {
                if (isSelectMode && isWithinBelarusBounds(lat, lng)) {
                    clearMarkers();
                    selectedLocation = new GeoPoint(lat, lng);
                    addMarker(lat, lng, "Выбранное место", lastSearchedAddress != null ? lastSearchedAddress : "", false, false);
                } else if (!isWithinBelarusBounds(lat, lng)) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Выберите точку в пределах РБ", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @JavascriptInterface
        public void onMessage(String message) {
            Log.d(TAG, "JavaScript Message: " + message);
        }

        @JavascriptInterface
        public void onSaveMarker(double lat, double lng, String address) {
            requireActivity().runOnUiThread(() -> {
                if (isWithinBelarusBounds(lat, lng)) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    EventModel eventModel = new EventModel();
                    eventModel.setTitle("Новое место");
                    eventModel.setCoordinates(new GeoPoint(lat, lng));
                    eventModel.setCity(parseCity(address));
                    eventModel.setAddress(address);
                    db.collection("events").add(eventModel.toMap())
                            .addOnSuccessListener(documentReference -> {
                                eventModel.setEventId(documentReference.getId());
                                eventList.add(eventModel);
                                Toast.makeText(requireContext(), "Маркер добавлен", Toast.LENGTH_SHORT).show();
                                updateMapAndList();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }

        @JavascriptInterface
        public void onMarkerUpdate(int index, double lat, double lng, String title, String address) {
            requireActivity().runOnUiThread(() -> {
                if (isWithinBelarusBounds(lat, lng) && index >= 0 && index < eventList.size()) {
                    EventModel event = eventList.get(index);
                    event.setTitle(title);
                    event.setCoordinates(new GeoPoint(lat, lng));
                    event.setCity(parseCity(address));
                    event.setAddress(address);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("events").document(event.getEventId())
                            .set(event.toMap())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Маркер обновлен", Toast.LENGTH_SHORT).show();
                                updateMapAndList();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }

        @JavascriptInterface
        public void requestMarkers() {
            requireActivity().runOnUiThread(() -> {
                try {
                    JSONArray markersArray = new JSONArray();
                    for (int i = 0; i < eventList.size(); i++) {
                        EventModel event = eventList.get(i);
                        JSONObject markerJson = new JSONObject();
                        markerJson.put("lat", event.getCoordinates().getLatitude());
                        markerJson.put("lng", event.getCoordinates().getLongitude());
                        markerJson.put("title", event.getTitle());
                        markerJson.put("snippet", "Расстояние: " + String.format("%.2f", userLocation != null ? calculateDistance(userLocation, event.getCoordinates()) : 0) + " км");
                        markerJson.put("isUserMarker", false);
                        markerJson.put("isEventMarker", currentEventLocation != null &&
                                event.getCoordinates().getLatitude() == currentEventLocation.getLatitude() &&
                                event.getCoordinates().getLongitude() == currentEventLocation.getLongitude());
                        markersArray.put(markerJson);
                    }
                    JSONObject response = new JSONObject();
                    response.put("action", "loadMarkers");
                    response.put("markers", markersArray);
                    webView.evaluateJavascript("window.postMessage(" + response.toString() + ", '*')", null);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending markers: " + e.getMessage(), e);
                }
            });
        }
    }
}