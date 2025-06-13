# RuralEdgeHealth

**Offline-First Edge AI and IoT System for Healthcare Monitoring on Entry-Level Smartphones in Resource-Constrained Communities**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)

## 🎯 Overview

RuralEdgeHealth is an innovative healthcare monitoring system that brings AI-powered health assessment to underserved populations without requiring internet connectivity. The system addresses the healthcare needs of **2.6 billion people globally** who lack reliable internet access.

## ✨ Key Features

- 🔄 **Offline-First Architecture**: Complete functionality without internet connectivity
- 📱 **Entry-Level Hardware Support**: Optimized for 2GB RAM Android 8.0+ devices  
- 🤖 **Clinical-Grade AI**: 98.25% classification accuracy with <100ms inference time
- 🔗 **IoT Integration**: Bluetooth Low Energy sensor connectivity with manual fallback
- 🔋 **Energy Efficient**: <4% battery consumption per hour for sustained field operation
- 🌍 **Accessibility Focus**: Designed for diverse user populations and literacy levels

## 📊 Dataset

The synthetic healthcare IoT dataset used for training and validation is available in the [`data/`](data/) folder:

- **File**: [`healthcare_iot_dataset.csv`](data/healthcare_iot_dataset.csv)
- **Records**: 10,000 synthetic health monitoring records
- **Features**: 15 features including vital signs, device metrics, and contextual information
- **Classes**: Healthy (50%), Moderate (35%), Critical (15%)

For detailed dataset documentation, see [`data/README.md`](data/README.md).

## 🏗️ Architecture

- **Presentation Layer**: User interface with BLE integration and manual input
- **Business Logic Layer**: Data processing, AI inference, and clinical context
- **Data Layer**: Local storage with encryption and model caching
- **Infrastructure Layer**: Android runtime with ONNX optimization

## 🔬 Research Results

| Metric | Achievement | Hardware |
|--------|-------------|----------|
| Classification Accuracy | 98.25% | 2GB RAM Android 8.0+ |
| Inference Time | <100ms | Entry-level smartphones |
| Battery Consumption | <4%/hour | Sustained field operation |
| System Usability (Healthcare Workers) | 85.6 SUS | Professional deployment |

## 📄 Research Paper

This repository accompanies our research paper:

**"RuralEdgeHealth: Offline-First Edge AI and IoT System for Healthcare Monitoring on Entry-Level Smartphones in Resource-Constrained Communities"**

*Submitted to Internet of Things (Elsevier)*

### Key Contributions:
- First complete offline-first edge AI healthcare system on entry-level devices
- Systematic accessibility evaluation across diverse user populations  
- Comprehensive technical validation with quantified deployment metrics
- Open-source implementation supporting global research reproducibility

## 🚀 Getting Started

### Prerequisites
- Android Studio Meerkat (2024.3.1+)
- Android SDK API Level 26+
- Device with minimum 2GB RAM

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/abubakarwakili9/RuralEdgeHealth.git

Open in Android Studio
Sync project with Gradle files
Build and run on target device

📚 Citation
If you use this code or dataset in your research, please cite:
A. Wakili and S. Bakkali, "RuralEdgeHealth: Offline-First Edge AI and IoT System for Healthcare Monitoring on Entry-Level Smartphones in Resource-Constrained Communities," Intelligent Medicine, 2025, manuscript submitted for publication.

🔗 Links

Dataset: data/healthcare_iot_dataset.csv
Documentation: data/README.md
Paper: [Internet of Things]

👥 Authors

Abubakar Wakili - Lead Researcher - a.wakili@ueuromed.org
Sara Bakkali - Co-Researcher - s.bakkali@insa.ueuromed.org

School of Digital Engineering and Artificial Intelligence
Euromed University of Fez, Morocco

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

🤝 Contributing
We welcome contributions! Please feel free to submit issues, fork the repository, and create pull requests.

🌟 Impact
This research supports digital health equity by extending AI-driven healthcare monitoring to underserved populations globally, advancing UN Sustainable Development Goal 3 (Good Health and Well-being) and WHO Universal Health Coverage objectives.

Made with ❤️ for global health equity
