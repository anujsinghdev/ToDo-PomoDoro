
# 📝 AnujTodo

AnujTodo is a modern, feature-rich productivity application built entirely with **Kotlin** and **Jetpack Compose**. It goes beyond simple task management by integrating a focus timer (Pomodoro), smart organization (Folders/Lists), and a stunning UI featuring Glassmorphism, Mesh Gradients, and fluid animations.


## ✨ Key Features

### 🚀 Productivity & Task Management

  * **Smart Dashboard**: Quick access to "My Day" (Today/Overdue), "Completed", and "Archive" lists.
  * **Organization**: Create custom **Lists** and group them into **Folders**.
  * **Task Details**: Add due dates, repeat modes (Daily, Weekly, etc.), and flag important tasks.
  * **Drag & Drop**: Reorder lists and folders with a long-press.
  * **Sorting**: Sort tasks by Importance, Due Date, Creation Date, or Alphabetically.
  * **Search**: Global search functionality with a glowing UI effect to find tasks and lists instantly.

### ⏱️ Focus Mode (Pomodoro)

  * **Integrated Timer**: Built-in Pomodoro timer to boost productivity.
  * **Background Persistence**: Timer continues to track time accurately even when the app is minimized or closed.
  * **Custom Duration**: Easily edit focus sessions to fit your workflow.
  * **Visual Feedback**: Beautiful circular progress and status indicators.

### 🎨 UI/UX & Design

  * **Jetpack Compose**: 100% declarative UI.
  * **Glassmorphism**: Custom frosted glass bottom navigation bar.
  * **Mesh Gradients**: Fluid, animated gradient buttons and backgrounds.
  * **Fluid Animations**:
      * **Rubber Band Effect**: Custom overscroll physics on lists.
      * **Liquid Buttons**: Buttons that animate like liquid.
      * **Spring Dialogs**: Bouncy, scale-in animations for alerts.
  * **Dark Mode**: Sleek, pure black aesthetic optimized for OLED screens.

## 🛠️ Tech Stack

  * **Language**: [Kotlin](https://kotlinlang.org/)
  * **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
  * **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
  * **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
  * **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
  * **Preferences**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Protobuf/Preferences)
  * **Concurrency**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
  * **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)

## 📂 Project Structure

The app follows a clean separation of concerns:

```text
com.anujsinghdev.anujtodo
├── data              # Data sources (Room DAO, DataStore, Repository Impl)
├── di                # Hilt Dependency Injection Modules
├── domain            # Business Logic (Models, Repository Interfaces, UseCases)
│   ├── model         # Data classes (TodoItem, TodoList, etc.)
│   ├── repository    # Repository Interfaces
│   └── usecase       # Domain Use Cases (AddTodo, GetTodos, etc.)
└── ui                # Presentation Layer (Screens, ViewModels, Components)
    ├── components    # Reusable UI (GlassBottomNav, MeshGradientButton, etc.)
    ├── login         # Authentication screens
    ├── todo_list     # Main Dashboard
    ├── list_detail   # Task list views
    ├── pomodoro      # Focus Timer
    └── theme         # App Theme & Color Palette
```

## 📸 Screenshots

| Login Screen | Dashboard | Pomodoro Timer |
|:---:|:---:|:---:|
|  |  |  |

*(Upload screenshots to your repo and link them here)*

## 📥 Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/yourusername/AnujTodo.git
    ```
2.  Open the project in **Android Studio** (Koala or newer recommended).
3.  Sync Gradle files.
4.  Run the app on an emulator or physical device.

## 🤝 Contribution

Contributions are welcome\! If you find a bug or want to add a feature:

1.  Fork the project.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.

-----

**Developed with ❤️ by Anuj Singh**
