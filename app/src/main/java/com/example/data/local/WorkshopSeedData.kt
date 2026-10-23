package com.example.data.local

import com.example.data.local.entity.DatasheetEntity
import com.example.data.local.entity.ItemEntity
import com.example.data.local.entity.PinoutEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScanEntity

object WorkshopSeedData {

  fun getInitialItems(): List<ItemEntity> = listOf(
    ItemEntity(
      name = "NE555 Precision Timer",
      category = "IC / Timer",
      quantity = 8,
      minStock = 3,
      location = "Drawer A-01",
      packageType = "DIP-8",
      specifications = "4.5V-16V, 200mA Out, Astable/Monostable",
      pinoutRef = "NE555",
      salvageSource = "Old Alarm Clock PCB / CRT TV",
      condition = "Tested / 100% OK"
    ),
    ItemEntity(
      name = "LM7805 Linear Regulator",
      category = "Voltage Regulator",
      quantity = 4,
      minStock = 2,
      location = "Drawer A-03",
      packageType = "TO-220",
      specifications = "+5V Output, 1.5A Max, Vin up to 35V",
      pinoutRef = "LM7805",
      salvageSource = "ATX PC Power Supply secondary rail",
      condition = "Desoldered with heatsink"
    ),
    ItemEntity(
      name = "IRFZ44N Power N-MOSFET",
      category = "Transistor / MOSFET",
      quantity = 1, // Trigger low stock warning!
      minStock = 3,
      location = "Drawer B-02",
      packageType = "TO-220",
      specifications = "55V, 49A, Rds(on) 17.5mΩ, Logic level friendly",
      pinoutRef = "IRFZ44N",
      salvageSource = "UPS Inverter Board 12V-230V",
      condition = "Low Stock Alert"
    ),
    ItemEntity(
      name = "ESP32-WROOM-32 MCU",
      category = "Microcontroller",
      quantity = 3,
      minStock = 1,
      location = "Reel Box IoT",
      packageType = "Module-38",
      specifications = "Dual-core Xtensa 240MHz, 4MB Flash, Wi-Fi & BLE",
      pinoutRef = "ESP32",
      salvageSource = "Smart Home Plug / IoT Gateway",
      condition = "Functional / Firmware erased"
    ),
    ItemEntity(
      name = "ATmega328P-PU",
      category = "Microcontroller",
      quantity = 2,
      minStock = 2,
      location = "Drawer A-05",
      packageType = "DIP-28",
      specifications = "8-bit AVR, 32KB Flash, 20MHz max, Optiboot compatible",
      pinoutRef = "ATmega328P",
      salvageSource = "Broken Arduino Uno Clone",
      condition = "Tested with ISP programmer"
    ),
    ItemEntity(
      name = "PC817 Optocoupler",
      category = "Optoelectronics",
      quantity = 12,
      minStock = 4,
      location = "SMD Box C-01",
      packageType = "DIP-4",
      specifications = "Phototransistor output, 5kV Isolation, If=20mA",
      pinoutRef = "PC817",
      salvageSource = "Salvaged Laptop Chargers & SMPS",
      condition = "Tested / Good isolation"
    ),
    ItemEntity(
      name = "1N4007 Rectifier Diode",
      category = "Diode",
      quantity = 25,
      minStock = 10,
      location = "Bag D-12",
      packageType = "DO-41",
      specifications = "1000V Peak Reverse, 1A Forward Current",
      pinoutRef = "1N4007",
      salvageSource = "General Appliance E-Waste",
      condition = "Tested with multimeter diode mode"
    ),
    ItemEntity(
      name = "Low-ESR 1000µF 25V Electrolytic",
      category = "Capacitor",
      quantity = 6,
      minStock = 4,
      location = "Drawer C-04",
      packageType = "Radial 10x16mm",
      specifications = "105°C rated, ESR < 0.05Ω",
      pinoutRef = "CAP_POL",
      salvageSource = "Motherboard VRM filter stage",
      condition = "Tested with LCR meter (ESR 0.03Ω)"
    )
  )

  fun getInitialPinouts(): List<PinoutEntity> = listOf(
    PinoutEntity(
      componentModel = "NE555",
      packageType = "DIP-8",
      description = "Classic Precision Timer & Oscillator IC",
      pinCount = 8,
      supplyVoltage = "4.5V - 16V DC",
      typicalApp = "PWM generator, LED flasher, Debounce circuit, Tone generator",
      pinsJson = """[
        {"pin":1,"label":"GND","type":"POWER","desc":"Ground reference (0V)"},
        {"pin":2,"label":"TRIG","type":"INPUT","desc":"Trigger input (active < 1/3 Vcc)"},
        {"pin":3,"label":"OUT","type":"OUTPUT","desc":"Output up to 200mA source/sink"},
        {"pin":4,"label":"RESET","type":"INPUT","desc":"Active-low reset (tie to Vcc if unused)"},
        {"pin":5,"label":"CTRL","type":"ANALOG","desc":"Control voltage (bypass with 10nF to GND)"},
        {"pin":6,"label":"THR","type":"INPUT","desc":"Threshold input (trips at > 2/3 Vcc)"},
        {"pin":7,"label":"DISCH","type":"OUTPUT","desc":"Open collector discharge for capacitor timing"},
        {"pin":8,"label":"VCC","type":"POWER","desc":"Positive supply voltage (+5V to +15V)"}
      ]"""
    ),
    PinoutEntity(
      componentModel = "LM7805",
      packageType = "TO-220",
      description = "Positive 3-Terminal 5V Voltage Regulator",
      pinCount = 3,
      supplyVoltage = "Vin: 7V - 25V DC (Output 5.0V ±4%)",
      typicalApp = "5V Logic board supply, Microcontroller power rails",
      pinsJson = """[
        {"pin":1,"label":"INPUT","type":"POWER_IN","desc":"Unregulated DC input (min 7V, max 35V)"},
        {"pin":2,"label":"GND","type":"POWER","desc":"Common ground (connected to metal tab)"},
        {"pin":3,"label":"OUTPUT","type":"POWER_OUT","desc":"Regulated +5.0V DC (up to 1.5A with heatsink)"}
      ]"""
    ),
    PinoutEntity(
      componentModel = "IRFZ44N",
      packageType = "TO-220",
      description = "N-Channel HEXFET Power MOSFET 55V 49A",
      pinCount = 3,
      supplyVoltage = "Vds max 55V, Vgs ±20V",
      typicalApp = "High-current DC motor switching, Solenoid driver, Inverter bridges",
      pinsJson = """[
        {"pin":1,"label":"GATE","type":"INPUT","desc":"Gate control (needs 10V for min Rds, 10k pulldown recommended)"},
        {"pin":2,"label":"DRAIN","type":"POWER","desc":"Drain (connected to load and metal heat tab)"},
        {"pin":3,"label":"SOURCE","type":"POWER","desc":"Source (connected to circuit Ground/Return)"}
      ]"""
    ),
    PinoutEntity(
      componentModel = "ESP32-WROOM-32",
      packageType = "Module-38",
      description = "Espressif Wi-Fi & Bluetooth Dual-Core SoC",
      pinCount = 38,
      supplyVoltage = "3.0V - 3.6V (Strict 3.3V logic)",
      typicalApp = "IoT sensors, Wireless telemetry, Web servers, Bluetooth BLE remotes",
      pinsJson = """[
        {"pin":1,"label":"3V3","type":"POWER","desc":"3.3V DC Power in (min 500mA peak capacity)"},
        {"pin":2,"label":"EN","type":"INPUT","desc":"Chip enable / Reset (active high, 10k pullup)"},
        {"pin":3,"label":"VP / GPIO36","type":"ADC","desc":"ADC1_CH0 input only (no internal pullup)"},
        {"pin":4,"label":"VN / GPIO39","type":"ADC","desc":"ADC1_CH3 input only (no internal pullup)"},
        {"pin":14,"label":"GND","type":"POWER","desc":"Ground reference"},
        {"pin":23,"label":"GPIO15","type":"GPIO","desc":"Strapping pin / HS2_CMD / PWM"},
        {"pin":24,"label":"GPIO2","type":"GPIO","desc":"Strapping pin / Onboard blue LED (NodeMCU)"},
        {"pin":25,"label":"GPIO0","type":"GPIO","desc":"Boot mode select (GND to flash firmware)"},
        {"pin":26,"label":"GPIO4","type":"GPIO","desc":"General purpose IO / ADC2_CH0 / Touch0"},
        {"pin":34,"label":"RXD0","type":"UART","desc":"UART0 Receive / Serial Monitor"},
        {"pin":35,"label":"TXD0","type":"UART","desc":"UART0 Transmit / Serial Monitor"},
        {"pin":38,"label":"GND","type":"POWER","desc":"Ground reference"}
      ]"""
    ),
    PinoutEntity(
      componentModel = "PC817",
      packageType = "DIP-4",
      description = "High Density Phototransistor Optocoupler",
      pinCount = 4,
      supplyVoltage = "Input If 20mA, Output Vceo 35V",
      typicalApp = "Galvanic feedback in SMPS power supplies, Relay isolation",
      pinsJson = """[
        {"pin":1,"label":"ANODE","type":"INPUT","desc":"Internal infrared LED Anode (+)"},
        {"pin":2,"label":"CATHODE","type":"INPUT","desc":"Internal infrared LED Cathode (-)"},
        {"pin":3,"label":"EMITTER","type":"OUTPUT","desc":"Phototransistor Emitter"},
        {"pin":4,"label":"COLLECTOR","type":"OUTPUT","desc":"Phototransistor Collector"}
      ]"""
    )
  )

  fun getInitialProjects(): List<ProjectEntity> = listOf(
    ProjectEntity(
      title = "Benchtop Lab Power Supply z ATX",
      description = "Přestavba vyřazeného 450W PC zdroje na regulovatelný dílenský zdroj 3.3V, 5V, 12V a nastavitelný 1.2-24V přes DC-DC step-up/down modul.",
      status = "IN_PROGRESS",
      tags = "ATX, PowerSupply, Salvage",
      neededParts = "Vypínač 230V, Zátěžový odpor 10Ω 10W (pro 5V větev), 4mm banánkové zdířky, Panelové voltmetry",
      notes = "Zelený vodič PS_ON propojen na GND přes přepínač. Výstup 12V otestován do 15A bez zvlnění.",
      priority = "HIGH"
    ),
    ProjectEntity(
      title = "Smart Soldering Fume Extractor",
      description = "Automatické odsávání zplodin z pájení s ventilátorem z PC skříně (120mm PWM), HEPA filtrem a senzorem kouře MQ-2.",
      status = "IDEA",
      tags = "Fan, Filter, ESP32, Safety",
      neededParts = "120mm fan 12V, Aktivní uhlíkový filtr, ESP32 pro automatické sepnutí, IRFZ44N MOSFET pro PWM",
      notes = "Použít starý 12V adaptér od Wi-Fi routeru pro napájení ventilátoru.",
      priority = "NORMAL"
    ),
    ProjectEntity(
      title = "Komponentový Tester LCR ze starého displeje",
      description = "Kompaktní tranzistor / ESR / LCR tester v 3D tisknuté krabičce se záchranou 16x2 LCD z vyřazené kopírky.",
      status = "COMPLETED",
      tags = "LCR, ATmega328P, Tool",
      neededParts = "ATmega328P, 16x2 LCD, ZIF patice, 9V bateriový konektor",
      notes = "Hotovo! Firmware nahrán přes Arduino ISP. Přesně detekuje pinout BJT i ESR elektrolytických kondenzátorů.",
      priority = "NORMAL"
    )
  )

  fun getInitialDatasheets(): List<DatasheetEntity> = listOf(
    DatasheetEntity(
      title = "NE555 Timer IC Datasheet & Engineering Notes",
      fileName = "TI_NE555_Datasheet_RevD.pdf",
      category = "IC",
      summary = "Bipolar timing circuit capable of producing accurate time delays or oscillation.",
      keyParameters = "Vcc: 4.5V-16V | Max sink/source: 200mA | Timing: µs to hours",
      chunksJson = """[
        {
          "chunkIndex": 1,
          "section": "1. Elektrická specifika a limity",
          "text": "Maximální napájecí napětí Vcc nesmí překročit 16V (18V absolutní maximum u verzí SA555). Výstupní pin 3 dokáže spínat proud až do 200mA, avšak při proudech nad 100mA se doporučuje sledovat ohřev pouzdra DIP-8."
        },
        {
          "chunkIndex": 2,
          "section": "2. Astabilní zapojení (Oscilátor)",
          "text": "Frekvence kmitů f = 1.44 / ((R1 + 2*R2) * C). Pro střídu blízkou 50% je vhodné překlenout rezistor R2 rychlou signální diodou (např. 1N4148), což umožní oddělené nabíjení přes R1 a vybíjení přes R2 a pin 7."
        },
        {
          "chunkIndex": 3,
          "section": "3. Diagnostika a e-waste testování",
          "text": "Při ověřování zachráněných čipů NE555 změřte klidový odběr proudu: bez zátěže by měl být 3mA až 6mA při 5V. Pokud čip odebírá více než 20mA nebo se zahřívá bez připojené zátěže, došlo k proražení vnitřního komparátoru."
        }
      ]"""
    ),
    DatasheetEntity(
      title = "LM78xx Series Linear Regulators Reference",
      fileName = "Fairchild_LM7805_Spec.pdf",
      category = "VOLTAGE_REG",
      summary = "Standard monolithic 3-terminal positive voltage regulator.",
      keyParameters = "Vout: 5.0V ±4% | Vin range: 7V-35V | Dropout: 2.0V typ",
      chunksJson = """[
        {
          "chunkIndex": 1,
          "section": "1. Dropout napětí a chlazení",
          "text": "Pro správnou regulaci na 5V musí vstupní napětí Vin činit alespoň 7.0V (dropout činí typicky 2.0V při 1A). Ztrátový výkon P = (Vin - 5V) * Iout. Při Vin=12V a proudu 0.5A je ztrátový výkon 3.5W a vyžaduje chladič s tepelným odporem pod 15°C/W."
        },
        {
          "chunkIndex": 2,
          "section": "2. Filtrace a stabilita",
          "text": "Na vstupu je striktně doporučen keramický kondenzátor 0.33µF umístěný do vzdálenosti 1cm od pouzdra. Na výstupu kondenzátor 0.1µF keramika + 10µF tantal/elektrolyt pro zabránění vysokofrekvenčním kmitům."
        },
        {
          "chunkIndex": 3,
          "section": "3. Záchrana a alternativy",
          "text": "Pokud je lineární regulátor příliš horký, ideální pinově kompatibilní moderní náhradou je spínaný DC-DC modul na bázi MP1584 nebo TSR 1-2450 (účinnost přes 90%, netopí)."
        }
      ]"""
    ),
    DatasheetEntity(
      title = "IRFZ44N HEXFET Power MOSFET Technical Guide",
      fileName = "Infineon_IRFZ44N_AppNote.pdf",
      category = "MOSFET",
      summary = "Fifth Generation HEXFETs utilize advanced processing techniques to achieve extremely low on-resistance per silicon area.",
      keyParameters = "Vds: 55V | Id: 49A | Rds(on): 17.5mΩ | Vgs(th): 2.0V - 4.0V",
      chunksJson = """[
        {
          "chunkIndex": 1,
          "section": "1. Řízení Gate a buzení",
          "text": "Ačkoliv prahové napětí Vgs(th) leží mezi 2V a 4V, pro plné sepnutí na deklarovaný odpor Rds(on) 17.5mΩ je nutné budit Gate napětím 10V. Při buzení přímo z 3.3V nebo 5V mikrokontroléru MOSFET zůstává v lineárním režimu a při velkém proudu okamžitě shoří."
        },
        {
          "chunkIndex": 2,
          "section": "2. E-Waste testování multimetrem",
          "text": "Postup testu funkčnosti: 1. Přepněte multimetr na diodový test. 2. Zkratujte všechny 3 piny pro vybití hradla. 3. Černý hrot na Drain, červený na Source – měřák ukáže úbytek vestavěné body-diody (cca 0.5V). 4. Červený hrot na Gate (nabijte vnitřní kapacitu) a vraťte na Drain – kanál je nyní sepnutý a měřák pípne/ukáže 0V!"
        },
        {
          "chunkIndex": 3,
          "section": "3. Ochrana hradla",
          "text": "Vždy připojte rezistor 10kΩ mezi Gate a Source (pulldown), aby hradlo neplavalo ve vzduchu. Pro spínání indukčních zátěží (motory, relé) je povinná antiparalelní rychlá dioda (flyback) přes zátěž."
        }
      ]"""
    ),
    DatasheetEntity(
      title = "E-Waste Bezpečnost: Spínané zdroje (SMPS) a Li-Ion baterie",
      fileName = "Workshop_Safety_Standard_E-Waste.pdf",
      category = "SAFETY",
      summary = "Bezpečnostní směrnice pro demontáž a repasování elektronického odpadu.",
      keyParameters = "VN kondenzátory: 400V DC | Li-Ion podvybití: < 2.5V = riziko dendritů",
      chunksJson = """[
        {
          "chunkIndex": 1,
          "section": "1. Vybití primárních kondenzátorů v SMPS",
          "text": "Před jakýmkoliv dotykem DPS spínaného zdroje (PC ATX, nabíječky) změřte napětí na velkém filtračním kondenzátoru (obvykle 400V/450V). Nikdy nevybíjejte šroubovákem napřímo! Použijte vybíjecí sondu s odporem 1kΩ až 2kΩ / 10W nebo klasickou 230V wolframovou žárovku 40W."
        },
        {
          "chunkIndex": 2,
          "section": "2. Záchrana 18650 článků z notebookových baterií",
          "text": "Pokud má článek 18650 napětí pod 2.0V, nepokoušejte se jej rychle dobíjet vysokým proudem. V článku se mohly vytvořit měděné dendrity schopné způsobit vnitřní zkrat a požár. Články s nulovým napětím mohou mít vypnutý CID (proudový přerušovač)."
        }
      ]"""
    )
  )

  fun getInitialScans(): List<ScanEntity> = listOf(
    ScanEntity(
      componentName = "SMPS PWM Controller UC3842B",
      packageType = "SOIC-8",
      confidence = 0.96f,
      specsSummary = "Current Mode PWM Controller, Vcc up to 30V, 500kHz max osc",
      pinCount = 8,
      functionalRole = "Primární řízení spínaného zdroje (Flyback)",
      salvageRating = "High",
      desolderDifficulty = "Moderate",
      reflectionVerified = true,
      isDemo = true,
      rawAnalysis = "Detekován SOIC-8 obvod UC3842 v napájecí větvi monitoru. Spolupracuje s optočlenem PC817 a výkonovým MOSFETem K2645.",
      imageUri = null
    )
  )
}
