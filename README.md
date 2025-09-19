# Android Fansub Creator

A comprehensive Android application for creating professional fansubs (fan-made subtitles) with advanced editing capabilities. This app provides all the essential tools needed for subtitle creation, positioning, styling, and timing without requiring internet connectivity.

## ✨ Features

### Core Functionality
- **Full ASS Subtitle Support** - Advanced Substation Alpha format with complete styling capabilities
- **Video Playback** - ExoPlayer integration with real-time subtitle overlay
- **Multi-Panel Interface** - Professional editing interface similar to Aegisub
- **Voice Recognition** - Automatic speech detection with timing bars and waveform visualization
- **Translation Engine** - Both online (Google Translate) and offline translation support
- **Drag & Drop Positioning** - Touch-based subtitle positioning directly on video
- **FFMPEG Integration** - Advanced video processing capabilities

### Advanced Editing Tools
- **Text Clipboard System** - Import/export predefined subtitle lines from text files
- **Timing Controls** - Precise timing adjustments with forward/backward shift support
- **Style Management** - Complete font, color, size, outline, and shadow customization
- **Font Management** - Support for both system and custom font installation
- **Real-time Preview** - Live subtitle rendering during video playback
- **Batch Operations** - Apply changes to multiple subtitles simultaneously

### Export & Compatibility
- **Multiple Formats** - Export to ASS, SRT, and other subtitle formats
- **File Management** - Storage Access Framework integration for seamless file operations
- **Project Management** - Save and load complete fansub projects

## 🚀 Installation

### Option 1: Download APK (Recommended)
1. Go to the [Releases](../../releases) section
2. Download the latest `fansub-creator.apk`
3. Install on your Android device
4. Grant necessary permissions (storage, microphone)

### Option 2: Build from Source
If you have Android development environment:
```bash
git clone https://github.com/your-repo/fansub-creator
cd fansub-creator
./gradlew assembleDebug
```

## 🎯 Quick Start Guide

1. **Launch the App** and grant required permissions
2. **Select Video** - Choose your anime/movie file
3. **Add Subtitles** - Tap "Add Sub" during playback to create subtitles at current time
4. **Position & Style** - Drag subtitles on video and customize appearance
5. **Use Voice Recognition** - Record audio to automatically detect speech timing
6. **Translate** - Use built-in translation for multiple languages
7. **Export** - Save your fansubs in ASS or SRT format

## 🎨 Interface Overview

### Multi-Panel Layout
- **Left Panel**: Video player with subtitle overlay and voice recognition
- **Center Panel**: Event list, timeline controls, and timing shift tools
- **Right Panel**: Text editor, style controls, and clipboard system
- **Bottom Panel**: Translation interface and batch operations

### Key Controls
- **Play/Pause**: Video playback control
- **Add Sub**: Create subtitle at current time
- **Record**: Start/stop voice recognition
- **Timing Shift**: Adjust all subtitles forward/backward
- **Translate**: Access translation panel
- **Export**: Save subtitles in various formats

## 🌐 Translation Support

### Supported Languages
- English 🇺🇸
- Japanese 🇯🇵  
- Korean 🇰🇷
- Chinese 🇨🇳
- Spanish 🇪🇸
- French 🇫🇷
- German 🇩🇪
- Italian 🇮🇹
- Portuguese 🇵🇹
- Russian 🇷🇺
- Arabic 🇸🇦
- And many more...

### Translation Modes
- **Online**: Google Translate API for high accuracy
- **Offline**: Built-in dictionary for common phrases
- **Manual**: Direct text editing and clipboard support

## 📱 Requirements

- **Android 7.0** (API level 24) or higher
- **2GB RAM** minimum, 4GB recommended
- **Storage Permission** for video and subtitle files
- **Microphone Permission** for voice recognition
- **Internet Connection** for online translation (optional)

## 🛠️ Advanced Features

### ASS Subtitle Styling
- Custom fonts with TTF/OTF support
- Primary, secondary, outline, and shadow colors
- Text scaling, spacing, and rotation
- Border styles and shadow effects
- Margin and alignment controls
- Layer management for complex scenes

### Voice Recognition
- Real-time waveform visualization  
- Automatic speech segment detection
- Adjustable sensitivity settings
- Export recognized segments as subtitles
- Manual timing adjustment support

### Professional Tools
- **Timeline Scrubbing**: Frame-accurate positioning
- **Keyboard Shortcuts**: Speed up workflow
- **Batch Processing**: Apply changes to multiple subtitles
- **Quality Control**: Built-in validation and error checking
- **Backup System**: Automatic project backup

## 📄 File Format Support

### Input Formats
- **Video**: MP4, MKV, AVI, MOV, WMV, FLV, WebM, M4V
- **Subtitles**: ASS, SSA, SRT, VTT, SUB
- **Text**: TXT for clipboard import
- **Fonts**: TTF, OTF

### Export Formats
- **ASS**: Advanced Substation Alpha (recommended)
- **SRT**: SubRip format for compatibility
- **VTT**: WebVTT for web use

## 🚨 Important Notes

- This app is designed for **personal use** and **fan translations**
- Always respect copyright laws in your region
- The app works **completely offline** except for online translation
- **No internet required** for core functionality
- Supports both **landscape and portrait** orientations

## 🆘 Troubleshooting

### Common Issues
1. **Video won't play**: Check if video format is supported
2. **Subtitles not showing**: Verify ASS style settings
3. **Voice recognition not working**: Check microphone permissions
4. **Translation fails**: Try offline mode or check internet connection
5. **Export issues**: Ensure sufficient storage space

### Performance Tips
- Close other apps for better performance
- Use lower resolution videos if experiencing lag  
- Regularly save your project to prevent data loss
- Clear app cache if experiencing issues

## 🤝 Contributing

This project is open source and contributions are welcome! Please feel free to:
- Report bugs and suggest features
- Submit pull requests for improvements
- Share your fansub creations with the community
- Help translate the app interface

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

## ❤️ Acknowledgments

- **Aegisub Team** - Inspiration for professional subtitle editing
- **ExoPlayer Team** - Excellent Android video player library
- **FFMPEG Team** - Video processing capabilities
- **Fansub Community** - Dedication to bringing content to global audiences

---

**Made with ❤️ for the fansub community**