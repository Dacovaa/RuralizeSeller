# UI/UX Redesign - Phase 1: Foundation & Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the app's visual identity to "Agro-Modern" and implement Bottom Navigation.

**Architecture:** Use a single `MainActivity` (Java) as a host for 4 core Fragments (Dashboard, Catalog, Sales, Profile) switched via a `BottomNavigationView`.

**Tech Stack:** Java, XML Layouts, Material Design 3, FragmentManager.

---

### Task 1: Update Visual Identity (Colors & Themes)

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: Update colors.xml with Agro-Modern palette**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="bg_ice">#F8FAFB</color>
    <color name="white">#FFFFFF</color>
    <color name="green_dark">#2F5D39</color>
    <color name="green_mid">#3D7C4A</color>
    <color name="green_light">#A8C7A1</color>
    <color name="text_primary">#2F5D39</color>
    <color name="text_secondary">#9AA39A</color>
    <color name="error_red">#FF5252</color>
    <color name="surface_border">#E0E0E0</color>
</resources>
```

- [ ] **Step 2: Update themes.xml with new background and card styles**

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.Ruralize" parent="Theme.Material3.Light.NoActionBar">
        <item name="android:windowBackground">@color/bg_ice</item>
        <item name="colorPrimary">@color/green_dark</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSurface">@color/white</item>
        <item name="colorOnSurface">@color/text_primary</item>
        
        <item name="materialButtonStyle">@style/Ruralize.Button</item>
        <item name="textInputStyle">@style/Ruralize.TextInput</item>
    </style>

    <style name="Ruralize.Card" parent="Widget.Material3.CardView.Elevated">
        <item name="cardCornerRadius">14dp</item>
        <item name="cardElevation">2dp</item>
        <item name="cardBackgroundColor">@color/white</item>
    </style>

    <style name="Ruralize.Button" parent="Widget.Material3.Button">
        <item name="cornerRadius">14dp</item>
        <item name="backgroundTint">@color/green_dark</item>
        <item name="android:textColor">@color/white</item>
    </style>
</resources>
```

- [ ] **Step 3: Commit visual updates**
```bash
git add app/src/main/res/values/colors.xml app/src/main/res/values/themes.xml
git commit -m "style: update visual identity to Agro-Modern"
```

---

### Task 2: Create Bottom Navigation Menu & Layout

**Files:**
- Create: `app/src/main/res/menu/bottom_nav_menu.xml`
- Create: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: Define bottom_nav_menu.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_dashboard"
        android:icon="@drawable/ic_menu"
        android:title="Início" />
    <item
        android:id="@+id/nav_catalog"
        android:icon="@drawable/ic_produtos"
        android:title="Catálogo" />
    <item
        android:id="@+id/nav_sales"
        android:icon="@drawable/ic_vendas"
        android:title="Vendas" />
    <item
        android:id="@+id/nav_profile"
        android:icon="@drawable/ic_conta"
        android:title="Conta" />
</menu>
```

- [ ] **Step 2: Create activity_main.xml with BottomNavigationView and Fragment Container**
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/white"
        app:title="Ruralize"
        app:titleTextColor="@color/green_dark" />

    <FrameLayout
        android:id="@+id/fragment_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_above="@+id/bottom_navigation"
        android:layout_below="@id/toolbar" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:background="@color/white"
        app:menu="@menu/bottom_nav_menu"
        app:itemIconTint="@color/green_dark"
        app:itemTextColor="@color/green_dark" />
</RelativeLayout>
```

- [ ] **Step 3: Commit layout changes**
```bash
git add app/src/main/res/menu/bottom_nav_menu.xml app/src/main/res/layout/activity_main.xml
git commit -m "feat: add bottom navigation layout"
```

---

### Task 3: Implement MainActivity (Java) & Navigation Logic

**Files:**
- Create: `app/src/main/java/com/example/ruralize/MainActivity.java`
- Delete: `app/src/main/java/com/example/ruralize/MainActivity.kt`

- [ ] **Step 1: Implement MainActivity.java to handle tab switching**
```java
package com.example.ruralize;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.ruralize.ui.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else {
                // Temporary stubs for other fragments
                selectedFragment = new DashboardFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }
}
```

- [ ] **Step 2: Remove old Kotlin MainActivity**
Run: `rm app/src/main/java/com/example/ruralize/MainActivity.kt`

- [ ] **Step 3: Commit MainActivity implementation**
```bash
git add app/src/main/java/com/example/ruralize/MainActivity.java
git rm app/src/main/java/com/example/ruralize/MainActivity.kt
git commit -m "feat: implement MainActivity in Java with fragment navigation"
```

---

### Task 4: Create Redesigned Dashboard Fragment

**Files:**
- Create: `app/src/main/res/layout/fragment_dashboard.xml`
- Create: `app/src/main/java/com/example/ruralize/ui/DashboardFragment.java`

- [ ] **Step 1: Define fragment_dashboard.xml with Summary Cards and Activity List**
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/bg_ice">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Resumo de Hoje"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="@color/green_dark" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:orientation="horizontal">
            <!-- Simplified Summary Cards -->
            <com.google.android.material.card.MaterialCardView
                style="@style/Ruralize.Card"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="8dp">
                <LinearLayout android:padding="12dp" android:orientation="vertical" android:layout_width="match_parent" android:layout_height="wrap_content">
                    <TextView android:text="Vendas" android:textColor="@color/text_secondary" android:textSize="12sp" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                    <TextView android:id="@+id/txtTotalVendas" android:text="R$ 0,00" android:textStyle="bold" android:textSize="16sp" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            
            <com.google.android.material.card.MaterialCardView
                style="@style/Ruralize.Card"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1">
                <LinearLayout android:padding="12dp" android:orientation="vertical" android:layout_width="match_parent" android:layout_height="wrap_content">
                    <TextView android:text="Pedidos" android:textColor="@color/text_secondary" android:textSize="12sp" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                    <TextView android:id="@+id/txtTotalPedidos" android:text="0" android:textStyle="bold" android:textSize="16sp" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Atividade Recente"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="@color/green_dark" />

        <com.google.android.material.card.MaterialCardView
            style="@style/Ruralize.Card"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp">
            <TextView
                android:padding="16dp"
                android:text="Nenhuma atividade recente encontrada."
                android:textColor="@color/text_secondary"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</ScrollView>
```

- [ ] **Step 2: Implement DashboardFragment.java migrating logic from DashboardActivity**
```java
package com.example.ruralize.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ruralize.R;

public class DashboardFragment extends Fragment {

    private TextView txtTotalVendas, txtTotalPedidos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        txtTotalVendas = view.findViewById(R.id.txtTotalVendas);
        txtTotalPedidos = view.findViewById(R.id.txtTotalPedidos);
        return view;
    }
}
```

- [ ] **Step 3: Commit Dashboard Fragment**
```bash
git add app/src/main/res/layout/fragment_dashboard.xml app/src/main/java/com/example/ruralize/ui/DashboardFragment.java
git commit -m "feat: implement redesigned Dashboard fragment"
```

---

### Task 5: Finalize Navigation & Set Launcher

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Set MainActivity as the Launcher and remove Activity launcher tag**
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.Ruralize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- Remove intent-filter from .Activity -->
<activity
    android:name=".Activity"
    android:exported="true"
    android:theme="@style/Theme.Ruralize">
</activity>
```

- [ ] **Step 2: Commit manifest changes**
```bash
git add app/src/main/AndroidManifest.xml
git commit -m "config: set MainActivity as the entry point"
```
