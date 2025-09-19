# Android Fansub Creator

## Overview
A comprehensive Android application for creating professional fansubs (fan-made subtitles) with advanced editing capabilities optimized for mobile devices. This app provides all the essential tools needed for subtitle creation, positioning, styling, and timing without requiring internet connectivity.

## Current State
**Status:** Core fansub creation features implemented and functional

The application includes:
- Video playback with ExoPlayer integration
- Draggable subtitle positioning with touch controls
- Comprehensive subtitle editing (text, timing, styling)
- Real-time subtitle overlay during video playback
- Subtitle timeline with progress tracking
- Export capabilities for SRT format

## Recent Changes (September 19, 2025)
- ✅ Set up complete Android project structure with Kotlin and Jetpack Compose
- ✅ Implemented SubtitleCue and SubtitleProject data models
- ✅ Created SubtitleViewModel for state management
- ✅ Built DraggableSubtitleOverlay component with touch gestures
- ✅ Developed SubtitleEditor dialog with full editing capabilities
- ✅ Enhanced VideoPlayerScreen with ExoPlayer integration
- ✅ Fixed navigation issues and compilation errors
- ✅ Implemented functional timing controls with time parsing

## User Preferences
- **Platform:** Android-only (user has only Android device, no PC/laptop)
- **Priority Features:** Fast subtitle creation, drag-and-drop positioning, voice recognition
- **Workflow:** Prefers local application over web-based tools for performance
- **Target Use:** Creating fansubs for anime and movies

## Project Architecture

### Core Components
- **MainActivity.kt:** Main entry point with navigation setup
- **SubtitleViewModel:** Manages subtitle project state and operations
- **VideoPlayerScreen:** Primary editing interface with video playback
- **DraggableSubtitleOverlay:** Touch-based subtitle positioning
- **SubtitleEditor:** Comprehensive editing dialog

### Data Models
- **SubtitleCue:** Individual subtitle with text, timing, position, and styling
- **SubtitleProject:** Container for video URI and subtitle collection

### Key Features Implemented
1. **Video Playback:** ExoPlayer integration with custom controls
2. **Subtitle Creation:** Add subtitles at current playback time
3. **Draggable Positioning:** Touch gestures for precise subtitle placement
4. **Style Editing:** Font size, color, and background customization
5. **Timing Controls:** Functional time parsing and validation
6. **Real-time Preview:** Active subtitles displayed during playback

### Technical Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Video Player:** ExoPlayer (Media3)
- **Build System:** Gradle with Android SDK
- **Architecture:** MVVM with Compose state management

## Next Steps (Pending Implementation)
1. Voice Activity Detection (VAD) for automatic timing
2. On-device speech recognition (Whisper/Vosk)
3. ASS/SRT import/export functionality
4. Storage Access Framework integration
5. GitHub Actions for APK building
6. Zoomable timeline interface

## Deployment
The project is configured as an Android application that can be built and installed on Android devices. The user will need to:
1. Build the APK using Android Studio or CI/CD
2. Install the APK on their Android device
3. Grant necessary permissions (storage, microphone)

## Performance Considerations
- Optimized for mobile hardware with efficient subtitle rendering
- Real-time playback state updates every 100ms
- Touch gesture handling optimized for responsiveness
- Memory-efficient subtitle overlay management