# RuralEdgeHealth Dataset

## Overview
The healthcare IoT dataset is used for developing and validating the RuralEdgeHealth offline-first edge AI system.

## Dataset Description
- **File**: `healthcare_iot_dataset.csv`
- **Records**: 10,000 synthetic health monitoring records
- **Features**: 15 features across 4 categories
- **Classes**: Healthy (50%), Moderate (35%), Critical (15%)

## Features Description

### Clinical Vitals (5 features)
- `HeartRate`: Heart rate in beats per minute
- `Temperature`: Body temperature in Celsius  
- `SystolicBP`: Systolic blood pressure in mmHg
- `DiastolicBP`: Diastolic blood pressure in mmHg
- `SpO2`: Blood oxygen saturation percentage

### Derived Clinical Metrics (3 features)
- `PulsePressure`: Difference between systolic and diastolic BP
- `HealthGap`: Calculated health deviation score
- `TargetBP`: Target blood pressure based on demographics

### Device & Context Metrics (4 features)
- `Device_Battery_Level`: IoT device battery percentage
- `Battery_Level`: Smartphone battery level
- `BatteryRatio`: Ratio of device to phone battery
- `Sensor_Location`: Placement of monitoring sensor

### Demographics & Lifestyle (3 features)
- `Gender`: Patient gender (binary encoded)
- `Activity_Level`: Physical activity level (categorical)
- `Medication_Status`: Current medication status

## Usage in Research

This dataset was used to:
1. Train Random Forest, XGBoost, LightGBM, and MLP models
2. Validate offline-first edge AI performance (98.25% accuracy)
3. Test cross-device compatibility on entry-level smartphones
4. Demonstrate clinical-grade performance on resource-constrained hardware

## Citation

If you use this dataset, please cite:
```bibtex
@misc{wakili2025ruraledgehealth_dataset,
  title={RuralEdgeHealth Synthetic Healthcare IoT Dataset},
  author={Wakili, Abubakar and Bakkali, Sara},
  year={2025},
  publisher={GitHub},
  url={https://github.com/abubakarwakili9/RuralEdgeHealth/tree/main/data}
}
