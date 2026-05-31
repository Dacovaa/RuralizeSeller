# Design Spec: UI/UX Redesign - Phase 1: Foundation & Navigation

**Date:** 2026-05-31
**Topic:** UI/UX Redesign (Agro-Modern Approach)
**Project:** RuralizeSeller (Android App)

---

## 1. Vision & Goals
The goal is to modernize the RuralizeSeller app, moving away from a dated "creme/heavy" visual style to a professional, clean, and functional "Agro-Modern" aesthetic. This phase focuses on the core navigation structure and the primary dashboard to improve usability and set the stage for upcoming features (notifications, detailed CRM).

## 2. Visual Identity (Agro-Modern)
- **Background:** Shift from `#F7F5E8` (creme) to `#F8FAFB` (cool white/ice) for a cleaner, more spacious feel.
- **Surface/Cards:** Pure white (`#FFFFFF`) backgrounds.
- **Elevation/Shadows:** Subtle borders (1dp, light gray) and soft shadows (elevation 2-4dp) to create depth without visual clutter.
- **Typography:** 
    - Titles: `ExtraBold`, dark green (`#2F5D39`).
    - Body: Regular, dark green or dark gray.
    - Secondary/Helper text: Medium gray (`#9AA39A`).
- **Shapes:** Standardized corner radius at `14dp` for a modern balanced look (less rounded than current `24dp`).

## 3. Architecture & Navigation
- **Primary Navigation:** Replace the Navigation Drawer with a **Bottom Navigation Bar**.
    - **Items:**
        1. **Dashboard (Home):** Performance summary and recent activity.
        2. **Catalog (Products):** Unified view for products and stock management.
        3. **Sales (Orders):** History of transactions and customer links.
        4. **Profile (Account):** User settings, credentials, and logout.
- **Top Bar (Toolbar):**
    - Left: Ruralize Logo.
    - Right: Notification Bell icon (new feature placeholder).
- **Navigation Model:** Use a single `MainActivity` as a host, switching between core views (Fragments or ViewVisibility) to ensure instant tab switching.

## 4. Redesigned Dashboard Layout
The Dashboard will be reorganized into a vertical scrolling view with the following sections:
1. **Summary Cards (Horizontal/Grid):** Three quick-glance metrics:
    - Total Sales (Month)
    - Orders (Today)
    - Out of Stock items
2. **Sales Graph:** Re-styled line or bar chart showing sales trends over the last 7 days.
3. **Recent Activity List:** A vertical list showing the last 3 system actions (e.g., "New order from [Client]", "Product [X] stock is low").
4. **Quick Actions (FAB or Buttons):** Direct access to "Add Product" and "New Sale".

## 5. Technical Impact
- **Themes:** Update `themes.xml` and `colors.xml` to reflect the new palette and styles.
- **Layouts:** Significant refactor of `activity_dashboard.xml` and `activity.xml` (or creating a new `activity_main.xml`).
- **Logic:** Refactor `BaseDrawerActivity` logic or replace it with a more modern navigation controller.
- **Backward Compatibility:** Maintain support for existing APIs (`OkHttp`) while the UI is being polished.

## 6. Success Criteria
- The app feels modern and "lighter" visually.
- Navigation between core modules (Products, Sales, Dashboard) is faster (fewer taps).
- Information hierarchy on the Dashboard is clear at a glance.
