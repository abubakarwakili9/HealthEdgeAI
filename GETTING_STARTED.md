# Getting Started with RuralEdgeHealth

**Complete Setup and Usage Guide for Offline-First Edge AI Healthcare Monitoring**

[![Phase 1](https://img.shields.io/badge/Development-Phase%201%20Validation-blue.svg)](README.md)
[![Documentation](https://img.shields.io/badge/Documentation-Complete-green.svg)]()

---

## 📋 Table of Contents

1. [Prerequisites & Requirements](#prerequisites--requirements)
2. [Installation & Setup](#installation--setup)
3. [Application Usage Guide](#application-usage-guide)
4. [Testing & Validation](#testing--validation)
5. [Phase 1 Capabilities](#phase-1-capabilities)
6. [Troubleshooting](#troubleshooting)
7. [Development & Contributing](#development--contributing)

---

## 🔧 Prerequisites & Requirements

### **Development Environment**
- **Android Studio**: Meerkat 2024.3.1+ (latest stable recommended)
- **Android SDK**: API Level 26+ (Android 8.0 Oreo)
- **Build Tools**: Gradle 8.5+, Kotlin 1.9.22+
- **JDK**: Version 17+ (included with Android Studio)

### **Target Hardware**
- **Minimum**: 2GB RAM, Android 8.0+ (API Level 26)
- **Recommended**: 4GB RAM, Android 10+ for optimal performance
- **Storage**: 500MB free space for app and data
- **Optional**: Bluetooth Low Energy (BLE) 4.0+ for sensor integration

### **Optional BLE Health Sensors**
- 🌡️ **Thermometer**: FDA-approved digital thermometers with BLE
- ❤️ **Heart Rate Monitor**: Chest straps or wrist-based monitors  
- 🩺 **Blood Pressure Cuff**: Automatic BLE-enabled cuffs
- 🫁 **Pulse Oximeter**: SpO2 sensors with Bluetooth connectivity

---

## 🚀 Installation & Setup

### **Step 1: Repository Setup**
```bash
# Clone the repository
git clone https://github.com/abubakarwakili9/RuralEdgeHealth.git
cd RuralEdgeHealth

# Verify repository structure
ls -la
# Expected: app/, data/, gradle/, build.gradle.kts, etc.
```

### **Step 2: Android Studio Setup**
1. **Open Project:**
   - Launch Android Studio
   - Select **"Open an existing project"**
   - Navigate to the cloned `RuralEdgeHealth` folder
   - Click **"OK"**

2. **Initial Configuration:**
   - Wait for **Gradle sync** to complete (~2-5 minutes)
   - Accept any SDK installation prompts
   - Ensure **Android SDK 26+** is installed
   - Verify **Kotlin plugin** is enabled

3. **Build Verification:**
   - Click **Build** → **Make Project** (Ctrl+F9)
   - Resolve any dependency issues if prompted
   - Successful build should show "BUILD SUCCESSFUL"

### **Step 3: Device Setup**

#### **Option A: Physical Android Device (Recommended)**
1. **Enable Developer Options:**
   - Go to **Settings** → **About Phone**
   - Tap **"Build Number"** 7 times
   - Return to **Settings** → **Developer Options**

2. **Enable USB Debugging:**
   - In Developer Options, enable **"USB Debugging"**
   - Connect device via USB cable
   - Accept debugging authorization on device

3. **Verify Connection:**
   - In Android Studio, check device appears in device selector
   - Device should show as "Connected" with model name

#### **Option B: Android Emulator**
1. **Create AVD:**
   - Tools → **AVD Manager** → **"Create Virtual Device"**
   - Select **Phone** category, choose device with 2GB+ RAM
   - **System Image**: API Level 26+ (Android 8.0+)
   - **Advanced Settings**: RAM 2048MB+, Internal Storage 4GB+

2. **Launch Emulator:**
   - Click **▶️ Play** button for created AVD
   - Wait for emulator to fully boot (~3-5 minutes)

### **Step 4: Deploy Application**
1. **Select Target:**
   - Choose your device/emulator from dropdown
   - Verify target shows as "Online"

2. **Run Application:**
   - Click **Run** (▶️) or press **Shift+F10**
   - First deployment may take 2-3 minutes
   - App should launch automatically on device

3. **Verify Installation:**
   - Look for **"RuralEdgeHealth"** icon on device
   - App should open without crashes
   - Check logcat for any error messages

---

## 📱 Application Usage Guide

### **Phase 1: Initial Launch & Setup**

#### **🚀 First Launch**
1. **Application Initialization:**
   - Launch **RuralEdgeHealth** from device
   - **Loading screen** appears (~2-3 seconds)
   - AI models and database initialize automatically
   - No internet connection required

2. **Welcome Screen:**
   - Brief system overview appears
   - **"Get Started"** button to proceed
   - Option to view **"About"** for system information

#### **👤 User Authentication**

**For New Users:**
1. **Registration Process:**
   - Tap **"Register"** or **"Create Account"**
   - Complete user profile form:
     - **Full Name**: Healthcare worker name
     - **Email**: Valid email address (for local account)
     - **Password**: Minimum 8 characters
     - **Role**: Healthcare Worker / Community Health Worker
     - **Organization**: Optional facility name
   - Tap **"Create Account"**
   - Account created locally (no server required)

**For Existing Users:**
1. **Login Process:**
   - Tap **"Login"** or **"Sign In"**
   - Enter registered **email** and **password**
   - Tap **"Sign In"**
   - Access granted to main dashboard

---

### **Phase 2: Patient Management**

#### **📋 Adding New Patients**
1. **Patient Registration:**
   - From main dashboard, tap **"Add Patient"** or **"+" button**
   - Complete patient information form:

   **Basic Information:**
   - **Full Name**: Patient's complete name
   - **Age**: Numeric age or date of birth
   - **Gender**: Male/Female/Other selection
   - **Contact**: Phone number (optional)
   - **Address**: Residential information

   **Medical Background:**
   - **Current Medications**: List of active prescriptions
   - **Known Conditions**: Diabetes, hypertension, etc.
   - **Allergies**: Drug or environmental allergies
   - **Emergency Contact**: Family member information

2. **Save Patient:**
   - Review entered information for accuracy
   - Tap **"Save Patient"** to store locally
   - Patient added to main patient list

#### **👥 Managing Existing Patients**
1. **Patient Selection:**
   - Browse patient list on main dashboard
   - Use **search function** to find specific patients
   - Tap patient name to **select for assessment**
   - Patient details load automatically

2. **Patient Information:**
   - View **complete patient profile**
   - Access **previous health assessments**
   - Edit patient information if needed
   - Export patient summary for referrals

---

### **Phase 3: Health Assessment Workflow**

#### **🏥 Starting Health Assessment**
1. **Assessment Initiation:**
   - With patient selected, tap **"Health Assessment"** 
   - Or tap **"New Assessment"** from patient profile
   - Assessment interface loads with empty forms

2. **Assessment Interface Overview:**
   - **Patient Info Panel**: Shows selected patient details
   - **Vital Signs Section**: Input fields for measurements
   - **Device Connection**: BLE sensor status indicators
   - **Manual Entry**: Slider controls for direct input
   - **AI Analysis**: Real-time processing status

#### **📊 Data Collection Methods**

**Method A: BLE Sensor Integration (Recommended)**
1. **Sensor Connection:**
   - Tap **"Connect Devices"** or Bluetooth icon
   - **Available Sensors** list appears:
     - 🌡️ **Digital Thermometer** (Body Temperature)
     - ❤️ **Heart Rate Monitor** (Pulse, SpO2)
     - 🩺 **Blood Pressure Cuff** (Systolic/Diastolic)
     - 🫁 **Pulse Oximeter** (Oxygen Saturation)

2. **Device Pairing:**
   - Ensure sensors are **powered on** and **discoverable**
   - Tap sensor name from available devices list
   - Follow **pairing prompts** on device screen
   - **Connection status** shows "Connected" when successful

3. **Automated Measurements:**
   - Position sensors according to manufacturer instructions
   - Tap **"Start Measurement"** for each connected device
   - **Real-time data** populates vital signs automatically
   - **Visual indicators** show measurement progress

**Method B: Manual Entry (Fallback Mode)**
1. **Manual Input Interface:**
   - If sensors unavailable, tap **"Manual Entry"**
   - **Intuitive sliders** with normal range indicators:

   **Vital Signs Input:**
   - **Heart Rate**: 60-120 bpm (color-coded ranges)
     - Green: 60-100 bpm (normal)
     - Yellow: 50-59, 101-120 bpm (caution)
     - Red: <50, >120 bpm (alert)
   
   - **Body Temperature**: 36-38°C with decimal precision
     - Normal: 36.1-37.2°C
     - Fever: >37.5°C
     - Hypothermia: <35°C
   
   - **Blood Pressure**: Systolic/Diastolic (mmHg)
     - Normal: <120/80
     - Elevated: 120-129/<80
     - High: ≥130/80
   
   - **SpO2**: Blood oxygen saturation (%)
     - Normal: 95-100%
     - Mild hypoxia: 90-94%
     - Severe: <90%
   
   - **Respiration Rate**: 12-20 breaths/minute

2. **Data Validation:**
   - **Real-time validation** prevents impossible values
   - **Warning indicators** for extreme measurements
   - **Confirmation dialogs** for critical values
   - **Save draft** option for incomplete assessments

#### **🤖 AI Health Classification**

1. **Triggering Analysis:**
   - Once all vital signs collected, tap **"Analyze Health Status"**
   - **Edge AI processing** begins immediately
   - **Progress indicator** shows analysis status (~50-100ms)
   - **No internet required** - all processing local

2. **Real-time Processing:**
   - **ONNX Random Forest model** processes 15 features
   - **Feature engineering** applies automatically
   - **Classification algorithms** evaluate health status
   - **Confidence scoring** provides reliability metrics

3. **Results Interpretation:**

   **🟢 Healthy Status:**
   - **Criteria**: All vital signs within normal ranges
   - **Recommendation**: Continue routine monitoring
   - **Follow-up**: Standard check-ups as scheduled
   - **Confidence**: Typically 85-95%

   **🟡 Moderate Risk:**
   - **Criteria**: 1-2 vital signs outside normal ranges
   - **Recommendation**: Enhanced monitoring required
   - **Follow-up**: Re-assess within 24-48 hours
   - **Interventions**: Lifestyle modifications, medication review
   - **Confidence**: Typically 75-90%

   **🔴 Critical Status:**
   - **Criteria**: Multiple critical indicators present
   - **Recommendation**: Immediate medical intervention
   - **Follow-up**: Emergency care or specialist referral
   - **Priority**: Urgent attention required
   - **Confidence**: Typically 80-95%

#### **📋 Clinical Decision Support**

1. **Detailed Analysis Report:**
   - **Vital Signs Summary**: All measurements with reference ranges
   - **Risk Factor Analysis**: Identified health concerns
   - **Clinical Recommendations**: Evidence-based guidance
   - **Trend Analysis**: Comparison with previous assessments
   - **Confidence Metrics**: AI model certainty levels

2. **Actionable Insights:**
   - **Immediate Actions**: Steps for healthcare provider
   - **Patient Education**: Health advice and lifestyle tips
   - **Monitoring Protocol**: Recommended follow-up schedule
   - **Referral Guidance**: When to seek specialist care

---

### **Phase 4: Data Management & Documentation**

#### **💾 Assessment Storage**
1. **Local Data Persistence:**
   - Tap **"Save Assessment"** to store results
   - **Encrypted storage** maintains patient privacy
   - **Offline accessibility** - no cloud dependencies
   - **Automatic backups** prevent data loss

2. **Assessment History:**
   - Access **"Patient History"** for previous assessments
   - **Chronological timeline** of health monitoring
   - **Trend visualization** shows health patterns
   - **Comparative analysis** between assessments

#### **📊 Reporting & Export**
1. **PDF Report Generation:**
   - Tap **"Generate Report"** for comprehensive summary
   - **Professional format** suitable for clinical records
   - **Patient identification** and assessment details
   - **Charts and graphs** for visual trend analysis

2. **Data Sharing Options:**
   - **Local sharing**: Via Bluetooth, USB, or local network
   - **Secure export**: Encrypted data packages
   - **Print capability**: Direct printing when available
   - **Integration ready**: Compatible with EMR systems

---

## 🧪 Testing & Validation

### **System Performance Testing**

#### **🔬 AI Model Validation**
1. **Accuracy Testing:**
   - **Expected**: 98.25% classification accuracy
   - **Test cases**: Use provided synthetic dataset
   - **Validation method**: Cross-device testing
   - **Performance metrics**: Precision, recall, F1-score

2. **Inference Speed Testing:**
   - **Target**: <100ms inference time
   - **Test conditions**: Entry-level devices (2GB RAM)
   - **Measurement**: Time from input to classification
   - **Consistency**: Multiple assessments should maintain speed

#### **🔋 Battery Performance Testing**
1. **Power Consumption Monitoring:**
   - **Target**: <4% battery consumption per hour
   - **Testing duration**: Continuous use for 2+ hours
   - **Usage scenarios**: Active assessment mode
   - **Device variations**: Test on multiple Android devices

2. **Energy Optimization Verification:**
   - **Background processing**: Minimal when idle
   - **Screen management**: Appropriate brightness handling
   - **Sensor management**: Efficient BLE connection handling

#### **📱 Cross-Device Compatibility**
1. **Hardware Testing Matrix:**
   - **Entry-level**: 2GB RAM, Android 8.0
   - **Mid-range**: 4GB RAM, Android 10+
   - **Various manufacturers**: Samsung, Xiaomi, Google, etc.
   - **Performance consistency**: Similar results across devices

### **Functional Testing Scenarios**

#### **🏥 Complete Workflow Testing**
1. **End-to-End Assessment:**
   ```
   Launch App → Login → Add Patient → Health Assessment → 
   Data Collection → AI Analysis → Results Review → Save
   ```

2. **Demo Patient Workflow:**
   - **Patient**: "Demo Patient, 45, Male"
   - **Vitals**: HR: 85, BP: 140/90, Temp: 37.2°C, SpO2: 95%
   - **Expected Result**: "Moderate Risk - Elevated Blood Pressure"
   - **Recommendations**: Blood pressure monitoring, lifestyle modifications

#### **🔌 Offline Functionality Testing**
1. **Connectivity Independence:**
   - **Airplane mode testing**: All features work without internet
   - **Network interruption**: Graceful handling of connection loss
   - **Local processing**: AI inference continues offline
   - **Data persistence**: Local storage maintains information

#### **🤝 BLE Integration Testing**
1. **Sensor Connection Testing:**
   - **Discovery process**: Sensors appear in available devices
   - **Pairing functionality**: Successful connection establishment
   - **Data transmission**: Accurate vital signs transfer
   - **Disconnection handling**: Graceful fallback to manual entry

---

## 🎯 Phase 1 Capabilities

### **✅ Validated Technical Achievements**

#### **AI Performance Validation**
- **✅ 98.25% Classification Accuracy**: Validated across 10,000 synthetic samples
- **✅ <100ms Inference Time**: Consistently achieved on 2GB RAM devices
- **✅ Cross-Device Performance**: Stable results across Android configurations
- **✅ Model Optimization**: ONNX deployment with ARM architecture optimization

#### **System Integration Validation**
- **✅ Offline-First Architecture**: Complete functionality without internet dependency
- **✅ BLE Sensor Integration**: Successful connectivity with health monitoring devices
- **✅ Manual Entry Fallback**: Intuitive interface for environments without sensors
- **✅ Local Data Storage**: Encrypted, persistent storage for patient information

#### **Usability Validation Results**
- **✅ Healthcare Workers**: 85.6 SUS score (Excellent usability)
- **✅ Task Completion**: 93.3% success rate across core functions
- **✅ Battery Efficiency**: <4% consumption per hour validated
- **⚠️ Rural Users**: 48.1 SUS score (Improvement needed for Phase 2)

### **🎯 Phase 1 Scope & Limitations**

#### **Current Capabilities**
- **Core Health Monitoring**: Vital signs collection and AI classification
- **Professional Interface**: Optimized for healthcare worker usage
- **Technical Validation**: Proven feasibility on accessible hardware
- **Research Foundation**: Open-source implementation for reproducibility

#### **Known Limitations (Phase 2 Priorities)**
- **Accessibility Enhancement**: Improved interface for low-literacy users
- **Advanced BLE Support**: Expanded sensor ecosystem compatibility
- **Clinical Documentation**: Enhanced reporting and EMR integration
- **Multi-language Support**: Localization for global deployment

#### **Development Status**
```
Phase 1: Technical Validation    ✅ COMPLETE
Phase 2: Accessibility Enhancement    🔄 PLANNED
Phase 3: Production Deployment    📋 FUTURE
```

---

## 🛠️ Troubleshooting

### **Common Installation Issues**

#### **🔴 Gradle Sync Failures**
**Problem**: Build fails with dependency errors
```
Solution:
1. Check internet connection for dependency downloads
2. File → Invalidate Caches and Restart
3. Update Android Studio to latest version
4. Verify JDK 17+ is configured
```

#### **🔴 Device Connection Issues**
**Problem**: Device not recognized by Android Studio
```
Solution:
1. Enable Developer Options on device
2. Enable USB Debugging in Developer Options
3. Install appropriate USB drivers for device
4. Try different USB cable/port
5. Restart ADB: adb kill-server && adb start-server
```

#### **🔴 Emulator Performance Issues**
**Problem**: Slow emulator or app crashes
```
Solution:
1. Allocate minimum 2GB RAM to AVD
2. Enable Hardware Acceleration (HAXM/Hyper-V)
3. Close other applications to free system memory
4. Use x86_64 system images for better performance
```

### **Runtime Application Issues**

#### **🔴 App Crashes on Startup**
**Problem**: Application force closes during launch
```
Diagnosis:
1. Check logcat for crash details
2. Verify device meets minimum requirements (2GB RAM)
3. Clear app data: Settings → Apps → RuralEdgeHealth → Storage → Clear Data
4. Reinstall application from Android Studio
```

#### **🔴 BLE Sensors Not Detected**
**Problem**: Bluetooth devices don't appear in sensor list
```
Solution:
1. Verify Bluetooth permissions granted to app
2. Ensure sensors are powered on and discoverable
3. Clear Bluetooth cache: Settings → Apps → Bluetooth → Storage → Clear Cache
4. Restart device and try again
5. Test manual entry mode as fallback
```

#### **🔴 Slow AI Inference**
**Problem**: Health classification takes >100ms
```
Optimization:
1. Close background applications to free RAM
2. Clear app cache and restart application
3. Verify device meets minimum specifications
4. Check for thermal throttling on device
```

#### **🔴 Data Not Saving**
**Problem**: Patient information or assessments not persisting
```
Solution:
1. Verify storage permissions granted
2. Check available device storage (minimum 500MB)
3. Force stop app and restart
4. Check for storage corruption: Clear app data and re-enter
```

### **Performance Optimization Tips**

#### **📈 Device Optimization**
- **Memory Management**: Close unnecessary background apps
- **Storage Management**: Maintain 1GB+ free space for optimal performance
- **Thermal Management**: Avoid overheating during extended use
- **Power Management**: Use device charger during intensive testing

#### **🔧 Application Optimization**
- **Regular Cache Clearing**: Prevents accumulated data issues
- **Periodic Restart**: Refreshes memory allocation
- **Update Management**: Keep Android Studio and SDK updated
- **Testing Environment**: Use dedicated device for development

---

## 👨‍💻 Development & Contributing

### **Development Environment Setup**

#### **🛠️ Advanced Development Tools**
```bash
# Additional useful tools for RuralEdgeHealth development
# Android Debug Bridge commands
adb devices                    # List connected devices
adb logcat | grep RuralEdge   # Filter app logs
adb shell dumpsys battery     # Monitor battery usage

# Gradle commands
./gradlew clean               # Clean project
./gradlew assembleDebug       # Build debug APK
./gradlew test               # Run unit tests
```

#### **📊 Performance Monitoring**
1. **Memory Profiling**: Use Android Studio Memory Profiler
2. **Battery Analysis**: Monitor with Battery Historian
3. **AI Performance**: Measure inference times in logcat
4. **Network Analysis**: Verify offline-first operation

### **Contributing Guidelines**

#### **🐛 Bug Reports**
1. **Issue Template**: Use GitHub issue templates
2. **Device Information**: Include Android version, RAM, manufacturer
3. **Reproduction Steps**: Clear step-by-step instructions
4. **Logs**: Attach relevant logcat output
5. **Expected vs Actual**: Describe the issue clearly

#### **💡 Feature Requests**
1. **Phase Alignment**: Consider Phase 1/2/3 roadmap
2. **Use Case**: Describe healthcare scenario and benefit
3. **Implementation**: Suggest technical approach if possible
4. **Accessibility**: Consider impact on diverse user populations

#### **🔍 Code Contributions**
1. **Fork Repository**: Create personal fork for development
2. **Feature Branches**: Use descriptive branch names
3. **Code Style**: Follow Kotlin coding conventions
4. **Testing**: Include unit tests for new functionality
5. **Documentation**: Update relevant documentation files

#### **🌍 Accessibility Testing**
1. **User Population Testing**: Test with diverse literacy levels
2. **Language Support**: Consider localization needs
3. **Interface Design**: Ensure inclusive design principles
4. **Feedback Collection**: Gather usability insights from target users

### **Research Collaboration**

#### **📚 Academic Integration**
- **Citation**: Use provided citation format for academic papers
- **Dataset Usage**: Follow dataset documentation for research use
- **Reproducibility**: Follow setup guide for result reproduction
- **Collaboration**: Contact authors for research partnerships

#### **🌐 Global Health Applications**
- **Deployment Consultation**: Guidance for healthcare organizations
- **Customization Support**: Adaptation for specific healthcare contexts
- **Training Materials**: Development of user training resources
- **Impact Assessment**: Collaboration on deployment effectiveness studies

---

## 📞 Support & Resources

### **📬 Contact Information**
- **Technical Support**: [a.wakili@ueuromed.org](mailto:a.wakili@ueuromed.org)
- **Research Collaboration**: [s.bakkali@insa.ueuromed.org](mailto:s.bakkali@insa.ueuromed.org)
- **General Inquiries**: GitHub Issues for public questions

### **📖 Additional Resources**
- **Main Repository**: [RuralEdgeHealth GitHub](https://github.com/abubakarwakili9/RuralEdgeHealth)
- **Research Paper**: Internet of Things Journal (Under Review)
- **Dataset Documentation**: [data/README.md](data/README.md)
- **License Information**: [MIT License](LICENSE)

### **🔗 External Resources**
- **Android Development**: [developer.android.com](https://developer.android.com)
- **ONNX Runtime**: [onnxruntime.ai](https://onnxruntime.ai)
- **Bluetooth LE**: [developer.android.com/guide/topics/connectivity/bluetooth-le](https://developer.android.com/guide/topics/connectivity/bluetooth-le)
- **Healthcare IoT**: [Internet of Things Journal](https://www.sciencedirect.com/journal/internet-of-things)

---

**🎉 Congratulations! You're now ready to explore RuralEdgeHealth and contribute to digital health equity through offline-first edge AI technology.**

*This Phase 1 validation successfully establishes technical feasibility and development priorities for advancing toward deployment-ready status, bringing clinical-grade AI healthcare monitoring to underserved populations worldwide.*

**[← Back to README](README.md)** | **[View Dataset Documentation →](data/README.md)**
