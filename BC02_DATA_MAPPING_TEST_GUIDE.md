# BC02 Data Mapping Test Guide

## 📋 Overview
This document provides a comprehensive guide for testing the BC02 data center image-to-node mapping functionality.

## 🎯 BC02 Mapping Requirements

### Image Order and Node Mapping
BC02 uses a specific image order with the following node mappings:

1. **NDP_INFO** → NDP transaction information
2. **NODE_INFO** → Node information  
3. **NODE_INFO_AETHIR** → Aethir node information
4. **SWITCH_100G** → 100G Switch [Non-clickable]
5. **LONOVO_POST** (Index 4) → BC02 Filecoin Miner (1번 lonovopost)
6. **LONOVO_POST** (Index 5) → BC02 3080Ti GPU Worker (2번 lonovopost)
7. **LONOVO_POST** (Index 6) → BC02 Post Worker (3번 lonovopost)
8. **NODE_MINER** → BC02 Filecoin Miner
9. **UPS_CONTROLLER** → UPS Controller [Non-clickable]
10. **STORAGE_1** (Index 9) → BC02 NAS1 (STORAGE_1)
11. **STORAGE_1** (Index 10) → BC02 NAS2 (STORAGE_1)
12. **STORAGE_1** (Index 11) → BC02 NAS3 (STORAGE_1)
13. **STORAGE_1** (Index 12) → BC02 NAS4 (STORAGE_1)
14. **STORAGE_1** (Index 13) → BC02 NAS5 (STORAGE_1)
15. **LOGO_ZETACUBE** → ZetaCube Logo [Admin access]

## 🧪 Test Scenarios

### 1. Data Center Selection Test
**Objective**: Verify BC02 data center can be selected and loads correct data

**Steps**:
1. Click on LOGO_ZETACUBE 8 times to access admin menu
2. Select "BC02" from the data center list
3. Verify the API call uses the correct NanoID: `5e807a27-7c3a-4a22-8df2-20c392186ed3`

**Expected Result**:
- Toast message: "Data center changed to: BC02"
- Images refresh with BC02-specific order
- API loads BC02 node data

### 2. LONOVO_POST Image Mapping Test
**Objective**: Verify three LONOVO_POST images map to correct nodes

**Test Cases**:

#### Test Case 2.1: First LONOVO_POST (Index 4)
- **Click**: 5th image (first LONOVO_POST)
- **Expected Card Title**: "BC02 Filecoin Miner (1번 lonovopost)"
- **Should Map To**: Node with name containing "Filecoin" AND "Miner"
- **Expected Node**: "BC02 Filecoin Miner"

#### Test Case 2.2: Second LONOVO_POST (Index 5)
- **Click**: 6th image (second LONOVO_POST)
- **Expected Card Title**: "BC02 3080Ti GPU Worker (2번 lonovopost)"
- **Should Map To**: Node with name containing "3080Ti" OR "GPU Worker"
- **Expected Node**: "BC02 3080Ti GPU Worker"

#### Test Case 2.3: Third LONOVO_POST (Index 6)
- **Click**: 7th image (third LONOVO_POST)
- **Expected Card Title**: "BC02 Post Worker (3번 lonovopost)"
- **Should Map To**: Node with name containing "Post Worker"
- **Expected Node**: "BC02 Post Worker"

### 3. STORAGE_1 Image Mapping Test
**Objective**: Verify five STORAGE_1 images map to correct NAS nodes

**Test Cases**:

#### Test Case 3.1: First STORAGE_1 (Index 9)
- **Click**: 10th image (first STORAGE_1)
- **Expected Card Title**: "BC02 NAS1 (STORAGE_1)"
- **Should Map To**: Node with name containing "NAS1"
- **Expected Node**: "BC02 NAS1"

#### Test Case 3.2: Second STORAGE_1 (Index 10)
- **Click**: 11th image (second STORAGE_1)
- **Expected Card Title**: "BC02 NAS2 (STORAGE_1)"
- **Should Map To**: Node with name containing "NAS2"
- **Expected Node**: "BC02 NAS2"

#### Test Case 3.3: Third STORAGE_1 (Index 11)
- **Click**: 12th image (third STORAGE_1)
- **Expected Card Title**: "BC02 NAS3 (STORAGE_1)"
- **Should Map To**: Node with name containing "NAS3"
- **Expected Node**: "BC02 NAS3"

#### Test Case 3.4: Fourth STORAGE_1 (Index 12)
- **Click**: 13th image (fourth STORAGE_1)
- **Expected Card Title**: "BC02 NAS4 (STORAGE_1)"
- **Should Map To**: Node with name containing "NAS4"
- **Expected Node**: "BC02 NAS4"

#### Test Case 3.5: Fifth STORAGE_1 (Index 13)
- **Click**: 14th image (fifth STORAGE_1)
- **Expected Card Title**: "BC02 NAS5 (STORAGE_1)"
- **Should Map To**: Node with name containing "NAS5"
- **Expected Node**: "BC02 NAS5"

### 4. Non-Clickable Image Test
**Objective**: Verify certain images don't show cards when clicked

**Test Cases**:
- Click SWITCH_100G (4th image) → No card should appear
- Click UPS_CONTROLLER (9th image) → No card should appear

### 5. Node Data Display Test
**Objective**: Verify node information is displayed correctly

**For each mapped node, verify**:
- Node name is displayed correctly
- Hardware specifications (CPU, RAM, Storage) are shown
- Usage percentages (CPU, Memory, Disk) are displayed
- Score information (if available) is shown

## 🔍 Debug Information

### Logging
The BC02DataMapper includes comprehensive logging. Check logcat for:
- `BC02DataMapper` tag for mapping logic
- `DataCenterComponents` tag for UI interactions

### Key Log Messages to Verify:
```
🔍 Finding BC02 node for imageIndex=4
   Target keyword: Filecoin Miner
   Available nodes: [BC02 Filecoin Miner, BC02 3080Ti GPU Worker, ...]
   
🎯 BC02 LONOVO_POST: Processing imageIndex=4
   Looking for Filecoin Miner
   Found Node: BC02 Filecoin Miner
   
🎨 Creating Storage Card:
   DisplayName: BC02 NAS1 (STORAGE_1)
   Node: BC02 NAS1
```

## 📊 API Response Validation

### Expected Node Names in BC02 Response:
1. BC02 Filecoin Miner
2. BC02 3080Ti GPU Worker  
3. BC02 Post Worker
4. BC02 NAS1
5. BC02 NAS2
6. BC02 NAS3
7. BC02 NAS4
8. BC02 NAS5

### Sample API Response Structure:
```json
{
  "nodes": [
    {
      "node_id": "cc5ca167-e17f-4766-9d11-a47cde13d0b2",
      "node_name": "BC02 Filecoin Miner",
      "nanodc_id": "5e807a27-7c3a-4a22-8df2-20c392186ed3"
    },
    {
      "node_id": "8f744157-9bdc-4afe-8783-e8b9a88fb71b", 
      "node_name": "BC02 3080Ti GPU Worker",
      "nanodc_id": "5e807a27-7c3a-4a22-8df2-20c392186ed3"
    }
    // ... more nodes
  ]
}
```

## ⚠️ Common Issues and Solutions

### Issue 1: Wrong Node Displayed
**Symptom**: Clicking an image shows data for wrong node
**Solution**: 
- Check image index in logs
- Verify BC02DataMapper mapping for that index
- Ensure node name matching logic is correct

### Issue 2: No Card Appears
**Symptom**: Clicking clickable image shows no card
**Solution**:
- Check if node was found in logs
- Verify API response contains expected node names
- Check if image type is in clickable list

### Issue 3: Default Names Instead of BC02 Names
**Symptom**: Cards show "GY01" names instead of "BC02"
**Solution**:
- Verify current data center is BC02
- Check nanoDcId in API calls
- Ensure BC02DataMapper is being used

## 🚀 Test Execution Checklist

- [ ] BC02 data center selection works
- [ ] All 3 LONOVO_POST images show correct nodes
- [ ] All 5 STORAGE_1 images show correct NAS nodes  
- [ ] NODE_MINER shows BC02 Filecoin Miner
- [ ] Non-clickable images don't show cards
- [ ] Node data displays correctly in cards
- [ ] Switching between data centers works properly
- [ ] API loads correct data for BC02

## 📝 Notes for Testers

1. **Image Counting**: Remember that image indices start from 0
2. **Card Content**: BC01/BC02 show full node info cards, GY01 shows disk usage only
3. **Auto Refresh**: Data refreshes automatically every 30 seconds
4. **Debug Mode**: Enable verbose logging in BC02DataMapper for detailed trace

## 🔧 Implementation Files

Key files involved in BC02 mapping:
- `/util/BC02DataMapper.kt` - Mapping logic
- `/data/ImageConfiguration.kt` - BC02 image order
- `/ui/component/DataCenterComponents.kt` - UI implementation
- `/network/api/NanoDcApiService.kt` - API calls
