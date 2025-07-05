# RuralEdgeHealth

**A Lightweight Edge-AI Framework for Offline Vital Signs Monitoring in Resource-Constrained IoT Healthcare Systems**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Phase](https://img.shields.io/badge/Development-Phase%201%20Validation-blue.svg)](GETTING_STARTED.md)

## 🎯 Overview

RuralEdgeHealth is an innovative healthcare monitoring system that brings AI-powered health assessment to underserved populations without requiring internet connectivity. The system addresses the healthcare needs of **2.6 billion people globally** who lack reliable internet access.

> **Phase 1 Status:** This foundational implementation successfully establishes technical feasibility and development priorities for advancing toward deployment-ready status. The system demonstrates sophisticated AI performance on entry-level devices while identifying specific accessibility improvements for equitable deployment.

## ✨ Key Features

- 🔄 **Offline-First Architecture**: Complete functionality without internet connectivity
- 📱 **Entry-Level Hardware Support**: Optimized for 2GB RAM Android 8.0+ devices  
- 🤖 **Model Performance**: 98.25% classification accuracy with <100ms inference time
- 🔗 **IoT Integration**: Bluetooth Low Energy sensor connectivity with manual fallback
- 🔋 **Energy Efficient**: <4% battery consumption per hour for sustained field operation
- 🌍 **Accessibility Focus**: Designed for diverse user populations and literacy levels

## 🚀 Quick Start

**New to RuralEdgeHealth?** 

📖 **[Complete Getting Started Guide →](GETTING_STARTED.md)**

### Rapid Setup (5 minutes)
```bash
# Clone and setup
git clone https://github.com/abubakarwakili9/RuralEdgeHealth.git
cd RuralEdgeHealth

# Open in Android Studio, sync Gradle, and run
# Detailed instructions: GETTING_STARTED.md
```

### Quick Test
1. 📱 Launch app → Register/Login
2. 👤 Add patient → Enter basic info  
3. 🏥 Health Assessment → Input vitals or connect BLE sensors
4. 🤖 View AI results → Real-time classification (<100ms)

**Need help?** See our **[detailed setup and usage guide](GETTING_STARTED.md)** with step-by-step instructions, troubleshooting, and demo workflows.

## 📊 Dataset

The healthcare IoT dataset used for training and validation is available in the [`data/`](data/) folder:

- **File**: [`healthcare_iot_dataset.csv`](data/healthcare_iot_dataset.csv)
- **Records**: 10,000 synthetic health monitoring records
- **Features**: 15 features including vital signs, device metrics, and contextual information
- **Classes**: Healthy (50%), Moderate (35%), Critical (15%)

For detailed dataset documentation, see [`data/README.md`](data/README.md).

## 🏗️ System Architecture

The architecture comprises a modular five-layer as follows:
- **Presentation and Interface Layer**: This provides an interactive user interface with multi-modal input support optimized for diverse user populations.
- **IoT Sensor Integration Layer**: The IoT Sensor Integration Layer manages standardized BLE medical device connectivity with intelligent fallback mechanisms.
- **Edge Intelligence & Processing Layer:**:This This layer orchestrates a lightweight AI pipeline optimized for resource-constrained inference.
- **Data Management & Persistence Layer**: This layer ensures Local storage with encryption and model caching. 
- **Infrastructure & Runtime  Layer**: The Infrastructure and the Runtime Layer provide cross-platform runtime optimization for diverse hardware configurations.
 
 
## 🔬 Phase 1 Validation Results

| Metric | Achievement | Hardware | Status |
|--------|-------------|----------|---------|
| Classification Accuracy | 98.25% | 2GB RAM Android 8.0+ | ✅ Validated |
| Inference Time | <100ms | Entry-level smartphones | ✅ Validated |
| Battery Consumption | <4%/hour | Sustained field operation | ✅ Validated |
| System Usability (Healthcare Workers) | 85.6 SUS | Professional deployment | ✅ Validated |
| Overall Task Completion | 93.3% | Cross-device testing | ✅ Validated |
| Accessibility (Rural Users) | 48.1 SUS | Requires enhancement | ⚠️ Phase 2 Priority |

### Key Phase 1 Achievements
- ✅ **Technical Feasibility Confirmed**: Clinical-grade AI operates successfully on entry-level devices
- ✅ **Cross-Device Validation**: Consistent performance across Android configurations  
- ✅ **Energy Efficiency Validated**: Sustained operation in resource-limited environments
- ✅ **Professional Usability**: Excellent acceptance among healthcare workers
- ⚠️ **Accessibility Insights**: Identified specific improvements needed for rural deployment

## 📄 Research Paper

This repository accompanies our research paper:

**"RuralEdgeHealth: A Lightweight Edge-AI Framework for Offline Vital Signs Monitoring in Resource-Constrained IoT Healthcare Systems"**

*Submitted to Array*

### Key Contributions:
- First complete offline-first edge AI healthcare system on entry-level devices
- Systematic accessibility evaluation across diverse user populations  
- Comprehensive technical validation with quantified deployment metrics
- Open-source implementation supporting global research reproducibility

## 📖 Documentation

| Document | Description | Audience |
|----------|-------------|----------|
| **[Getting Started Guide](GETTING_STARTED.md)** | Complete setup, usage, and testing instructions | Developers, Researchers |
| **[Dataset Documentation](data/README.md)** | Detailed dataset information and usage | Data Scientists, ML Engineers |
| **[API Documentation](docs/API.md)** | Technical API reference *(Coming Soon)* | Integration Developers |
| **[Deployment Guide](docs/DEPLOYMENT.md)** | Production deployment instructions *(Phase 2)* | Healthcare Organizations |

## 📊 Model Information

- **Algorithm**: Random Forest (optimized with ONNX)
- **Training Data**: 10,000 synthetic healthcare records
- **Validation**: 5-fold cross-validation with SMOTETomek balancing
- **Optimization**: ONNX Runtime v1.17.0 with ARM architecture optimization
- **Model Location**: `app/src/main/assets/random_forest_model.onnx`
- **Performance**: 98.25% accuracy, F1-score: 0.9825, ROC-AUC: 0.9989

## 🗺️ Development Roadmap

### ✅ Phase 1: Technical Validation (Current)
- Core offline-first edge AI system
- Cross-device performance validation
- Basic accessibility evaluation
- Open-source release for reproducibility

### 🔄 Phase 2: Accessibility Enhancement (Planned)
- Enhanced UI for rural/low-literacy users (Target: >75 SUS)
- Expanded BLE sensor ecosystem support
- Advanced clinical decision support features
- Comprehensive deployment documentation

### 🎯 Phase 3: Production Deployment (Future)
- Large-scale clinical validation studies
- Healthcare system integration APIs
- Multi-language support and localization
- Regulatory compliance frameworks

## 📚 Citation

If you use this code or dataset in your research, please cite:

```bibtex
@article{wakili2025ruraledgehealth,
  title={RuralEdgeHealth: A Lightweight Edge-AI Framework for Offline Vital Signs Monitoring in Resource-Constrained IoT Healthcare Systems},
  author={Wakili, Abubakar and Bakkali, Sara},
  journal={Array},
  year={2025},
  note={Manuscript submitted for publication},
  url={https://github.com/abubakarwakili9/RuralEdgeHealth}
}
```

## 🔗 Links

- **📊 Dataset**: [data/synthetic_healthcare_iot_dataset_enhanced.csv](data/synthetic_healthcare_iot_dataset_enhanced.csv)
- **📖 Getting Started**: [GETTING_STARTED.md](GETTING_STARTED.md)
- **📋 Documentation**: [data/README.md](data/README.md)
- **📄 Paper**: [Internet of Things Journal (Under Review)](https://www.sciencedirect.com/journal/internet-of-things)

## 👥 Authors

- **Abubakar Wakili** - *Lead Researcher* - [a.wakili@ueuromed.org](mailto:a.wakili@ueuromed.org)
- **Sara Bakkali** - *Co-Researcher* - [s.bakkali@insa.ueuromed.org](mailto:s.bakkali@insa.ueuromed.org)

*School of Digital Engineering and Artificial Intelligence*  
*Euromed University of Fez, Morocco*

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

We welcome contributions! This Phase 1 validation establishes the foundation for community-driven development:

- 🐛 **Bug Reports**: Help us improve system reliability
- 💡 **Feature Requests**: Suggest enhancements for Phase 2 development  
- 🔍 **Accessibility Testing**: Test with diverse user populations
- 📚 **Documentation**: Improve setup and usage guides
- 🌍 **Localization**: Add support for additional languages

Please see our **[Getting Started Guide](GETTING_STARTED.md)** for development setup instructions.

## 🌟 Impact & Recognition

This research supports **digital health equity** by extending AI-driven healthcare monitoring to underserved populations globally, advancing:

- 🎯 **UN Sustainable Development Goal 3**: Good Health and Well-being
- 🏥 **WHO Universal Health Coverage**: Equitable access to quality health services
- 🌍 **Digital Health Equity**: Accessible AI for 2.6 billion underserved people

### Phase 1 Validation Impact
- **Technical Foundation**: Proven feasibility of offline-first edge AI on accessible hardware
- **Research Reproducibility**: Open-source implementation enables global collaboration
- **Development Priorities**: Evidence-based roadmap for equitable deployment
- **Academic Contribution**: First complete system addressing digital health infrastructure barriers

---

*Made with ❤️ for global health equity*

**Ready to get started?** 🚀 **[Follow our complete setup guide →](GETTING_STARTED.md)**
