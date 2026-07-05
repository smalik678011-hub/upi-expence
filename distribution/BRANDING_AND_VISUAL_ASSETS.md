# UPI Unified Expense Tracker — Branding & Visual Asset Specifications

This guide establishes the brand parameters, color palettes, visual hierarchies, and detailed production specifications for the application’s Play Store graphical assets (App Icon, Feature Graphic, and Screenshots) to maintain professional consistency and Material 3 design compliance.

---

## Part 1: Brand System & Visual Identity

### 1. Naming & Brand Voice
- **Full Application Name**: UPI Unified Expense Tracker
- **Launcher Icon Short Name (under app icon)**: UPI Tracker
- **Tagline**: Automatic. Offline. 100% Private.
- **Brand Voice**: Objective, secure, reliable, and modern. We do not use hype, nor do we present ourselves as a bank or loan platform. We are a technical, local ledger that empowers users to own their financial data.

### 2. Design System: Emerald Slate Color Palette
We recommend a dark/slate hybrid aesthetic that is modern, readable, and highly distinct from the standard blue/purple fintech designs.

| Color Element | Light Mode Hex Value | Dark Mode Hex Value | Visual Intent |
| :--- | :--- | :--- | :--- |
| **Primary (Emerald)** | `#00875A` | `#10B981` | Accent actions, active credit, success states, FAB background. |
| **Secondary (Teal Slate)** | `#3A506B` | `#64748B` | Selected navigation items, borders, and group headings. |
| **Background (Midnight Slate)** | `#F8FAFC` | `#0F172A` | Base app canvas. Promotes extreme negative space. |
| **Surface (Steel Card)** | `#FFFFFF` | `#1E293B` | Base container card backgrounds. Deeply contrasted against background. |
| **Debit (High Contrast)** | `#DC2626` | `#EF4444` | Outgoing transaction flows (Sent), negative balance indicators. |
| **Credit (Emerald Glow)** | `#15803D` | `#10B981` | Incoming transaction flows (Received), positive cash flows. |

### 3. Typography Pairings
- **Display & Headings**: *Space Grotesk* or *Lexend* (Geometric, technical, modern, high-contrast weight for totals and cards).
- **Body & Captions**: *Inter* or *System default Roboto* (Clean, highly legible at small sizes, optimal for long ledger rows).
- **Numbers / Metrics**: *JetBrains Mono* (Monospace design that guarantees perfectly aligned decimals and numbers in columns).

---

## Part 2: App Icon Specifications (Google Play Ready)

The application launcher icon is the single most important brand identifier on a user's home screen. We utilize a highly compliant, Material Design 3 **Adaptive Icon** configuration.

```
       108dp Total Canvas (No important detail should bleed into this outer boundary)
     ┌─────────────────────────────────────────────────────────┐
     │                     72dp Icon Area                      │
     │                 ┌───────────────────────┐               │
     │                 │     66dp Safe Zone    │               │
     │                 │    ┌─────────────┐    │               │
     │                 │    │             │    │               │
     │                 │    │  (₹ Symbol) │    │               │
     │                 │    │             │    │               │
     │                 │    └─────────────┘    │               │
     │                 └───────────────────────┘               │
     │                                                         │
     └─────────────────────────────────────────────────────────┘
```

### 1. Foreground Layer Specifications (`ic_launcher_foreground.xml`)
- **Visual Design**: An elegant, bold, flat vector silhouette containing a geometric shield intersecting with a stylized **Rupee symbol (₹)**.
- **Safe Zone Rule**: Must fit strictly within the **66dp safe zone** (centered in the 108dp total canvas area) to prevent truncation when Google Play applies rounded-corner masks (such as squircle, circle, or teardrop shapes).
- **Styling**: Utilize sharp, non-overlapping vector nodes. No thin lines or delicate borders. The main element must have heavy visual weight to ensure readability even at small notification drawer scales.

### 2. Background Layer Specifications (`ic_launcher_background.xml`)
- **Visual Design**: A subtle, elegant linear gradient.
- **Gradient Coordinates**: `startX="0.0"`, `startY="0.0"`, `endX="108.0"`, `endY="108.0"` (Top-left to bottom-right diagonal sweep).
- **Colors**: Sweep from `#0F172A` (Midnight Slate) to `#1E293B` (Steel Slate). This deep, sleek background makes the foreground emerald-green icon pop with premium contrast.

### 3. Google Play Console Icon (512×512 px)
- **Dimensions**: 512 × 512 pixels.
- **Format**: 32-bit PNG with transparent alpha layer.
- **Maximum File Size**: 1024 KB.
- **Design Requirement**: Must be a high-resolution render of the adaptive icon. Do not include shadows, gloss effects, or banners that are not part of the active device icon. Ensure it is crisp, centered, and has high-contrast readability against both white and black home wallpapers.

---

## Part 3: Feature Graphic Specifications (1024×500 px)

The Feature Graphic is displayed prominently at the top of your Google Play Store listing. It acts as a marketing billboard.

- **Required Dimensions**: 1024 × 500 pixels.
- **Format**: 24-bit PNG (no alpha) or high-quality JPEG.
- **Maximum File Size**: 1024 KB.
- **Composition Guide**:
  1. **Safe Center Frame**: Place all crucial text, logos, and focal illustrations within a centralized **800 × 400 px safe area**. Google Play often overlays badges or crops edges on smaller screens.
  2. **Background**: A deep, rich Midnight Slate gradient (`#0F172A` to `#020617`) with faint geometric light lines radiating from behind the main subject.
  3. **Main Asset**: A sleek, isometric 3D render of a smartphone displaying the beautiful **Emerald Slate Dashboard** on the right side.
  4. **Value Proposition (Left-aligned, prominent, large type)**: 
     - **Title**: UPI tracker that respects your privacy.
     - **Sub-caption**: 100% Offline. Zero SMS permissions. Auto-tracks GPay, PhonePe, and Paytm.
  5. **Policy Compliance**: Do not include call-to-actions like "Download Now!", ratings stars, prices, or store badges (e.g., "Get it on Google Play") as these violate modern Google Play listing policies.

---

## Part 4: Google Play Store Screenshot Strategy

We recommend a **9-screen portrait sequence** for mobile, optimized to communicate core benefits over raw technical features. Each screenshot should consist of a styled device frame (framed in midnight slate) placed against a high-contrast backdrop, accompanied by a clean marketing heading at the top.

### Sequence & Visual Structure

#### Screenshot 1: The Welcome Hook
- **Marketing Heading**: 100% Private UPI Ledger
- **Device View**: The Onboarding Screen showing the "100% Private Offline Ledger" pledge.
- **Key Benefit Highlighted**: Establishes immediate trust. Shows that the app does not request banking details or dangerous permissions.

#### Screenshot 2: The Core Dashboard
- **Marketing Heading**: Automated Financial Visibility
- **Device View**: The interactive **Home Screen / Dashboard** displaying total income (Credit), expenses (Debit), and the net balance.
- **Key Benefit Highlighted**: Displays the dynamic, clean Emerald Slate Material 3 charts. Showcases visual scanning indicators.

#### Screenshot 3: Interactive Ledger
- **Marketing Heading**: Your UPI History in One Safe Place
- **Device View**: The **Transaction Explorer** list showing populated ledger items from Google Pay, PhonePe, and Paytm.
- **Key Benefit Highlighted**: Illustrates clean row designs, precise lakh number formatting, and color-coded transaction directions.

#### Screenshot 4: Advanced Offline Analytics
- **Marketing Heading**: See Where Your Money Flows
- **Device View**: The **Analytics Screen** displaying monthly category distributions, high-volume spending days, and merchant distributions.
- **Key Benefit Highlighted**: Demonstrates powerful local metrics, automated charts, and spending insights.

#### Screenshot 5: Instant Search & Filters
- **Marketing Heading**: Find Transactions in Seconds
- **Device View**: Search panel in Transaction Explorer filtered by "GPay" or specific date ranges.
- **Key Benefit Highlighted**: Emphasizes offline indexing speed and granular control over large ledgers.

#### Screenshot 6: Secure CSV Export
- **Marketing Heading**: Own Your Financial Records
- **Device View**: **Export Screen** with the "Save to Device" and "Share Report" controls.
- **Key Benefit Highlighted**: Highlights data portability. Demonstrates that users can export to local CSV files without uploading records online.

#### Screenshot 7: Backup & Restore
- **Marketing Heading**: Safe Local Backups
- **Device View**: **Backup & Restore Screen** showing offline encrypted database management.
- **Key Benefit Highlighted**: Shows that the user retains absolute control of their historical ledger data, with simple offline migration tools.

#### Screenshot 8: Secure Customizer
- **Marketing Heading**: Designed Around You
- **Device View**: **Appearance Settings Screen** showing theme options (Emerald Slate, Material You) and Hindi/English language toggle.
- **Key Benefit Highlighted**: Highlights Material 3 flexibility and localization readiness.

#### Screenshot 9: The Privacy Pledge
- **Marketing Heading**: No Servers, No Cloud, Zero Risk
- **Device View**: **Privacy Pledge Screen** outlining why the app doesn't request internet access to run, read SMS, or request bank credentials.
- **Key Benefit Highlighted**: Closes the loop on security, reinforcing why the app is the safest ledger in the Indian ecosystem.

---

## Part 5: Notification & Status Bar Icon Guidance

To ensure the notification icon matches the Android system aesthetics, follow these strict development parameters:

1. **Design**: The status bar notification icon must be **entirely white on a transparent background**. 
2. **Format**: Vector XML or flat transparent PNG.
3. **Strict Restriction**: No color layers, gradients, or shadows. Android automatically tints the notification icon depending on the active theme and notification context. Any colored pixels will render as blocky grey squares on modern Android API levels.
4. **Implementation**: Save the flat outline in `res/drawable/ic_notification_stat.xml` and pass it to the local Notification Builder as:
   ```kotlin
   .setSmallIcon(R.drawable.ic_notification_stat)
   ```
