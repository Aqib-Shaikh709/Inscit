package com.example.inscit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.*
import com.example.inscit.models.TopicDetail
import kotlin.math.*

@Composable
fun TtsController(
    text: String,
    tts: TTSManager,
    accent: Color,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            if (tts.isSpeaking) tts.stop() else tts.speak(text)
        }, modifier = Modifier.size(iconSize + 8.dp)) {
            if (tts.isSpeaking) {
                StopIcon(accent, Modifier.size(iconSize))
            } else {
                SpeakerIcon(accent, Modifier.size(iconSize))
            }
        }
    }
}

@Composable
fun InteractionControlPanel(
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(CardBg, RoundedCornerShape(24.dp))
            .border(1.dp, GhostWhite.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(accent.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SettingsIcon(accent, Modifier.size(14.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "INTERACTIVE PARAMETERS",
                color = GhostWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(GhostWhite.copy(alpha = 0.05f))
            )
        }
        
        Spacer(Modifier.height(20.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                .border(1.dp, accent.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun InteractionContainer(
    title: String,
    accent: Color,
    legend: List<Pair<String, Color>>,
    liveIndexes: List<Pair<String, String>>,
    controls: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Main Diagram Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg, RoundedCornerShape(28.dp))
                .border(1.dp, GhostWhite.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title.uppercase(),
                    color = GhostWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                        .padding(2.dp)
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                content()
                
                // Legend Corner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(DeepSpace.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, GhostWhite.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    legend.forEach { (label, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                            Box(Modifier.size(8.dp).background(color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 9.sp, color = GhostWhite.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Live Index Corner
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    liveIndexes.forEach { (key, value) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
                            Text(key, fontSize = 10.sp, color = accent, fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(6.dp))
                            Text(value, fontSize = 11.sp, color = GhostWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Separate Sub-Section for Controls
        if (controls != null) {
            InteractionControlPanel(accent) {
                controls()
            }
        }
    }
}

@Composable
fun InteractiveTopicDiagram(topic: TopicDetail, accent: Color) {
    when (topic.branch) {
        Branch.PHYSICS -> PhysicsInteractions(topic, accent)
        Branch.CHEMISTRY -> ChemistryInteractions(topic, accent)
        Branch.BIOLOGY -> BiologyInteractions(topic, accent)
    }
}

@Composable
fun PhysicsInteractions(topic: TopicDetail, accent: Color) {
    when (topic.id) {
        "p1" -> KinematicsInteraction(accent)
        "p2" -> NewtonsLawInteraction(accent)
        "p3" -> EnergyInteraction(accent)
        "p4" -> ReflectionInteraction(accent)
        "p5" -> HeatTransferInteraction(accent)
        "p6" -> WaveMechanicsInteraction(accent)
        "p7" -> ElectromagnetismInteraction(accent)
        "p8" -> NuclearPhysicsInteraction(accent)
        else -> DefaultPlaceholder(topic, accent)
    }
}

@Composable
fun ChemistryInteractions(topic: TopicDetail, accent: Color) {
    when (topic.id) {
        "c1" -> StatesOfMatterInteraction(accent)
        "c2" -> ElementsMixturesInteraction(accent)
        "c3" -> AtomModelInteraction(accent)
        "c4" -> ChemicalChangeInteraction(accent)
        "c5" -> AcidsBasesInteraction(accent)
        "c6" -> QuantumAtomicInteraction(accent)
        "c7" -> PeriodicTrendsInteraction(accent)
        "c8" -> ChemicalBondingInteraction(accent)
        else -> DefaultPlaceholder(topic, accent)
    }
}

@Composable
fun BiologyInteractions(topic: TopicDetail, accent: Color) {
    when (topic.id) {
        "b1" -> CellInteraction(accent)
        "b2" -> PlantTissueInteraction(accent)
        "b3" -> NutritionInteraction(accent)
        "b4" -> RespirationInteraction(accent)
        "b5" -> PlantReproductionInteraction(accent)
        "b6" -> EndocrineInteraction(accent)
        "b7" -> NervousSystemInteraction(accent)
        "b8" -> EcologyInteraction(accent)
        else -> DefaultPlaceholder(topic, accent)
    }
}

@Composable
fun ElectromagnetismInteraction(accent: Color) {
    var current by remember { mutableFloatStateOf(2f) }
    val rotation by rememberInfiniteTransition("em").animateFloat(0f, 360f, infiniteRepeatable(tween(2000, easing = LinearEasing)))

    InteractionContainer(
        title = "Electromagnetism: Field Lines",
        accent = accent,
        legend = listOf("Wire" to Color.Gray, "Magnetic Field" to accent, "Electron Flow" to NeonCyan),
        liveIndexes = listOf(
            "Current (I):" to "${"%.1f".format(current)} A",
            "B-Field:" to "${"%.2f".format(current * 0.5f)} T",
            "Dir:" to "Right-Hand Rule"
        ),
        controls = {
            Text("Adjust Current Intensity", color = GhostWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = current, onValueChange = { current = it }, valueRange = 0f..5f,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Wire
            drawRect(Color.Gray, Offset(center.x - 10f, 20f), Size(20f, size.height - 40f))
            
            // Electrons
            repeat(5) { i ->
                val ey = (System.currentTimeMillis() / 10 + i * 100) % (size.height - 60f) + 30f
                drawCircle(NeonCyan, radius = 4f, center = Offset(center.x, ey))
            }
            
            // Field Lines (Circular)
            repeat(3) { i ->
                val r = 50f + i * 40f
                val alpha = (1f - i/4f) * (current / 5f)
                drawCircle(accent.copy(alpha = alpha), radius = r, center = center, style = Stroke(2f))
                
                // Direction arrows
                val ang = rotation + i * 120f
                val ax = center.x + r * cos(Math.toRadians(ang.toDouble())).toFloat()
                val ay = center.y + r * sin(Math.toRadians(ang.toDouble())).toFloat()
                drawCircle(accent, radius = 4f, center = Offset(ax, ay))
            }
        }
    }
}

@Composable
fun NuclearPhysicsInteraction(accent: Color) {
    var isFission by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(if(isFission) 1f else 0f, tween(1000, easing = LinearEasing), label = "fission")

    InteractionContainer(
        title = "Nuclear Physics: Fission",
        accent = accent,
        legend = listOf("U-235 Nucleus" to accent, "Neutron" to Color.White, "Energy Release" to Color.Yellow),
        liveIndexes = listOf(
            "Process:" to if(isFission) "Fission" else "Stable",
            "Energy:" to if(isFission) "200 MeV" else "0",
            "Byproducts:" to if(isFission) "Ba + Kr" else "None"
        ),
        controls = {
            Button(
                onClick = { isFission = !isFission },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if(isFission) PowerRed else accent)
            ) {
                Text(if(isFission) "RESET REACTION" else "TRIGGER FISSION", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            if (progress < 0.1f) {
                // Large Nucleus
                drawCircle(accent, radius = 40f, center = center)
                drawCircle(Color.White.copy(alpha = 0.3f), radius = 45f, center = center, style = Stroke(2f))
                // Incoming Neutron
                drawCircle(Color.White, radius = 6f, center = Offset(40f, center.y))
            } else {
                // Fragments
                val dist = progress * 150f
                drawCircle(accent.copy(alpha = 0.7f), radius = 25f, center = Offset(center.x - dist, center.y - dist/2))
                drawCircle(accent.copy(alpha = 0.7f), radius = 20f, center = Offset(center.x + dist, center.y + dist/3))
                
                // Energy Burst
                drawCircle(Color.Yellow.copy(alpha = 1f - progress), radius = progress * 200f, center = center)
                
                // Released Neutrons
                repeat(3) { i ->
                    val ang = Math.toRadians((i * 120f).toDouble()).toFloat()
                    val nx = center.x + progress * 250f * cos(ang)
                    val ny = center.y + progress * 250f * sin(ang)
                    drawCircle(Color.White, radius = 4f, center = Offset(nx, ny))
                }
            }
        }
    }
}

@Composable
fun PeriodicTrendsInteraction(accent: Color) {
    var atomicNumber by remember { mutableFloatStateOf(1f) }
    
    InteractionContainer(
        title = "Periodic Trends",
        accent = accent,
        legend = listOf("Atom Radius" to accent, "Nuclear Pull" to PowerRed, "Shielding" to NeonCyan),
        liveIndexes = listOf(
            "Z-Eff:" to "${"%.1f".format(atomicNumber * 0.3f)}",
            "Radius:" to "${(200 / atomicNumber).toInt()} pm",
            "Electroneg:" to "${"%.1f".format(atomicNumber * 0.4f)}"
        ),
        controls = {
            Text("Atomic Number (Z)", color = GhostWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = atomicNumber, onValueChange = { atomicNumber = it }, valueRange = 1f..20f,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = PowerRed, activeTrackColor = PowerRed)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = 120f * (1f / (1f + atomicNumber * 0.1f))
            
            // Outer Boundary
            drawCircle(accent.copy(alpha = 0.1f), radius = radius, center = center)
            drawCircle(accent, radius = radius, center = center, style = Stroke(2f))
            
            // Nucleus (Grows with Z)
            drawCircle(PowerRed, radius = 10f + atomicNumber, center = center)
            
            // Attraction vectors
            repeat(8) { i ->
                val ang = Math.toRadians((i * 45f).toDouble()).toFloat()
                val start = Offset(center.x + (10f+atomicNumber)*cos(ang), center.y + (10f+atomicNumber)*sin(ang))
                val end = Offset(center.x + radius*cos(ang), center.y + radius*sin(ang))
                drawLine(PowerRed.copy(alpha = 0.3f), start, end, strokeWidth = 1f)
            }
        }
    }
}

@Composable
fun ChemicalBondingInteraction(accent: Color) {
    var isIonic by remember { mutableStateOf(true) }
    val progress by rememberInfiniteTransition("bond").animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)))

    InteractionContainer(
        title = "Chemical Bonding",
        accent = accent,
        legend = listOf("Atom A" to accent, "Atom B" to TechViolet, "Electron" to NeonCyan),
        liveIndexes = listOf(
            "Type:" to if(isIonic) "Ionic" else "Covalent",
            "Bond E:" to if(isIonic) "High" else "Medium",
            "Force:" to if(isIonic) "Electrostatic" else "Sharing"
        ),
        controls = {
            Button(
                onClick = { isIonic = !isIonic },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(if(isIonic) "SWITCH TO COVALENT" else "SWITCH TO IONIC", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            val leftPos = Offset(size.width / 3f, centerY)
            val rightPos = Offset(size.width * 2/3f, centerY)
            
            drawCircle(accent, radius = 30f, center = leftPos)
            drawCircle(TechViolet, radius = 30f, center = rightPos)
            
            if (isIonic) {
                // Transfer
                val ex = leftPos.x + (rightPos.x - leftPos.x) * progress
                drawCircle(NeonCyan, radius = 5f, center = Offset(ex, centerY))
                if (progress > 0.9f) drawCircle(NeonCyan, radius = 5f, center = rightPos)
            } else {
                // Sharing
                val ex = (leftPos.x + rightPos.x) / 2 + sin(progress * 2 * PI.toFloat()) * 40f
                drawCircle(NeonCyan, radius = 5f, center = Offset(ex, centerY))
            }
        }
    }
}

@Composable
fun EndocrineInteraction(accent: Color) {
    var isActive by remember { mutableStateOf(false) }
    val pulse by rememberInfiniteTransition("endo").animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)))

    InteractionContainer(
        title = "Endocrine System: Hormones",
        accent = accent,
        legend = listOf("Gland" to accent, "Hormone" to Color.Yellow, "Target Cell" to TechViolet),
        liveIndexes = listOf(
            "Status:" to if(isActive) "Secreting" else "Idle",
            "Signal:" to "Chemical",
            "Level:" to if(isActive) "Peak" else "Basal"
        ),
        controls = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if(isActive) "STOP SECRETION" else "START SECRETION", color = GhostWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Switch(checked = isActive, onCheckedChange = { isActive = it }, colors = SwitchDefaults.colors(checkedThumbColor = accent))
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            
            // Gland
            drawCircle(accent, radius = 30f, center = Offset(60f, centerY))
            drawCircle(accent.copy(alpha = 0.3f), radius = 30f + (if(isActive) pulse * 20f else 0f), center = Offset(60f, centerY), style = Stroke(2f))
            
            // Target Cell
            drawCircle(TechViolet, radius = 40f, center = Offset(size.width - 60f, centerY))
            
            if (isActive) {
                repeat(5) { i ->
                    val travel = (pulse + i * 0.2f) % 1f
                    val hx = 60f + (size.width - 120f) * travel
                    val hy = centerY + sin(travel * 10f) * 20f
                    drawCircle(Color.Yellow, radius = 4f, center = Offset(hx, hy))
                }
            }
        }
    }
}

@Composable
fun NervousSystemInteraction(accent: Color) {
    var triggerAction by remember { mutableStateOf(0) }
    val pulse by animateFloatAsState(if(triggerAction % 2 == 1) 1f else 0f, tween(800, easing = LinearEasing), label = "impulse")

    InteractionContainer(
        title = "Nervous System: Impulse",
        accent = accent,
        legend = listOf("Neuron" to accent, "Action Potential" to NeonCyan, "Synapse" to Color.White),
        liveIndexes = listOf(
            "State:" to if(pulse > 0) "Firing" else "Resting",
            "Voltage:" to if(pulse > 0) "+40 mV" else "-70 mV",
            "Speed:" to "120 m/s"
        ),
        controls = {
            Button(
                onClick = { triggerAction++ },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text("FIRE NEURAL IMPULSE", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            
            // Soma
            drawCircle(accent, radius = 30f, center = Offset(60f, centerY))
            // Axon
            drawLine(accent, Offset(90f, centerY), Offset(size.width - 60f, centerY), strokeWidth = 8f)
            // Terminals
            drawLine(accent, Offset(size.width - 60f, centerY), Offset(size.width - 20f, centerY - 20f), strokeWidth = 4f)
            drawLine(accent, Offset(size.width - 60f, centerY), Offset(size.width - 20f, centerY + 20f), strokeWidth = 4f)
            
            if (pulse > 0) {
                val ix = 60f + (size.width - 120f) * pulse
                drawCircle(NeonCyan, radius = 10f, center = Offset(ix, centerY))
                drawCircle(NeonCyan.copy(alpha = 0.3f), radius = 20f, center = Offset(ix, centerY))
            }
        }
    }
}

@Composable
fun EcologyInteraction(accent: Color) {
    var trophicLevel by remember { mutableFloatStateOf(1f) }

    InteractionContainer(
        title = "Ecology: Energy Pyramid",
        accent = BioLime,
        legend = listOf("Energy Level" to BioLime, "Loss (Heat)" to PowerRed, "Biomass" to accent),
        liveIndexes = listOf(
            "Level:" to when(trophicLevel.toInt()) { 1 -> "Producer"; 2 -> "Primary"; 3 -> "Secondary"; else -> "Tertiary" },
            "Energy:" to "${(1000 / (10f.pow(trophicLevel-1))).toInt()} kcal",
            "Efficiency:" to "10%"
        ),
        controls = {
            Text("Trophic Level Transition", color = GhostWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = trophicLevel, onValueChange = { trophicLevel = it }, valueRange = 1f..4f, steps = 2,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = BioLime, activeTrackColor = BioLime)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val level = trophicLevel.toInt()
            
            for (i in 1..4) {
                val width = 200f - (i-1) * 50f
                val height = 30f
                val py = size.height - 40f - (i-1) * 40f
                val color = if(i == level) BioLime else BioLime.copy(alpha = 0.2f)
                
                drawRect(color, Offset(center.x - width/2, py), Size(width, height))
                
                if (i == level && i < 4) {
                    // Heat loss arrow
                    drawLine(PowerRed, Offset(center.x + width/2, py + 15f), Offset(center.x + width/2 + 40f, py - 20f), strokeWidth = 2f)
                }
            }
        }
    }
}

@Composable
fun KinematicsInteraction(accent: Color) {
    var velocity by remember { mutableFloatStateOf(3f) }
    var acceleration by remember { mutableFloatStateOf(0.1f) }
    var time by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while(true) {
            withFrameMillis { 
                time += 0.016f // approx 60fps
                if (time > 10f) time = 0f
            }
        }
    }

    val displacement = velocity * time + 0.5f * acceleration * time * time
    val currentVelocity = velocity + acceleration * time

    InteractionContainer(
        title = "Kinematics: Constant Acceleration",
        accent = accent,
        legend = listOf("Position" to accent, "Velocity Vector" to PowerRed, "Reference Path" to Color.Gray.copy(alpha = 0.3f)),
        liveIndexes = listOf(
            "T:" to "${"%.1f".format(time)}s",
            "V:" to "${"%.1f".format(currentVelocity)}m/s",
            "S:" to "${"%.1f".format(displacement)}m"
        ),
        controls = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Initial Velocity", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = velocity, onValueChange = { velocity = it }, valueRange = 1f..10f, colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent))
                }
                Column(Modifier.weight(1f)) {
                    Text("Acceleration", color = PowerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = acceleration, onValueChange = { acceleration = it }, valueRange = 0f..2f, colors = SliderDefaults.colors(thumbColor = PowerRed, activeTrackColor = PowerRed))
                }
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            val scale = size.width / 150f
            val posX = (displacement * scale) % size.width
            
            // Road/Path
            drawLine(Color.Gray.copy(alpha = 0.3f), Offset(0f, centerY + 20f), Offset(size.width, centerY + 20f), strokeWidth = 4f)
            for(i in 0..10) {
                val markX = (i * size.width / 10f)
                drawLine(Color.Gray.copy(alpha = 0.2f), Offset(markX, centerY + 15f), Offset(markX, centerY + 25f), strokeWidth = 2f)
            }
            
            // Object
            drawCircle(accent, radius = 12f, center = Offset(posX, centerY))
            drawCircle(accent.copy(alpha = 0.2f), radius = 18f, center = Offset(posX, centerY), style = Stroke(2f))
            
            // Velocity Vector
            val arrowLen = currentVelocity * 5f
            drawLine(PowerRed, Offset(posX, centerY), Offset(posX + arrowLen, centerY), strokeWidth = 4f, cap = StrokeCap.Round)
            val path = Path().apply {
                moveTo(posX + arrowLen + 5f, centerY)
                lineTo(posX + arrowLen - 5f, centerY - 5f)
                lineTo(posX + arrowLen - 5f, centerY + 5f)
                close()
            }
            drawPath(path, PowerRed)
        }
    }
}

@Composable
fun NewtonsLawInteraction(accent: Color) {
    var force by remember { mutableFloatStateOf(50f) }
    var mass by remember { mutableFloatStateOf(10f) }
    val acceleration = force / mass
    var posX by remember { mutableFloatStateOf(0f) }
    var velX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(force, mass) {
        var lastTime = System.currentTimeMillis()
        while(true) {
            withFrameMillis {
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000f
                lastTime = now
                
                velX += acceleration * dt
                posX += velX * dt
                if (posX > 1000f) { posX = 0f; velX = 0f }
            }
        }
    }

    InteractionContainer(
        title = "Newton's 2nd Law: F = ma",
        accent = accent,
        legend = listOf("Applied Force" to PowerRed, "Mass Block" to accent, "Acceleration" to NeonCyan),
        liveIndexes = listOf(
            "F:" to "${force.toInt()}N",
            "M:" to "${mass.toInt()}kg",
            "A:" to "${"%.2f".format(acceleration)}m/s²"
        ),
        controls = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Applied Force (N)", color = PowerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = force, onValueChange = { force = it }, valueRange = 10f..100f, colors = SliderDefaults.colors(thumbColor = PowerRed, activeTrackColor = PowerRed))
                }
                Column(Modifier.weight(1f)) {
                    Text("Object Mass (kg)", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = mass, onValueChange = { mass = it }, valueRange = 5f..50f, colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent))
                }
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val boxSize = 30f + mass * 1.5f
            val centerY = size.height / 2f
            val x = (posX * 50f) % size.width
            
            // Surface
            drawLine(Color.DarkGray, Offset(0f, centerY + boxSize/2), Offset(size.width, centerY + boxSize/2), strokeWidth = 2f)
            
            // Mass Block
            drawRect(accent, Offset(x - boxSize/2, centerY - boxSize/2), Size(boxSize, boxSize))
            drawRect(accent.copy(alpha = 0.3f), Offset(x - boxSize/2, centerY - boxSize/2), Size(boxSize, boxSize), style = Stroke(2f))
            
            // Force Vector
            val forceLen = force * 1.2f
            drawLine(PowerRed, Offset(x - boxSize/2 - forceLen, centerY), Offset(x - boxSize/2, centerY), strokeWidth = 6f)
            val arrowPath = Path().apply {
                moveTo(x - boxSize/2, centerY)
                lineTo(x - boxSize/2 - 10f, centerY - 8f)
                lineTo(x - boxSize/2 - 10f, centerY + 8f)
                close()
            }
            drawPath(arrowPath, PowerRed)
            
            // Acceleration Visualizer (Particles)
            for (i in 0..3) {
                val offset = ((System.currentTimeMillis() / 10 % 100) + i * 25) % 100
                drawCircle(NeonCyan.copy(alpha = 1f - offset/100f), radius = 3f, center = Offset(x + boxSize/2 + 10f + offset * (acceleration/2f), centerY))
            }
        }
    }
}

@Composable
fun EnergyInteraction(accent: Color) {
    var gravity by remember { mutableFloatStateOf(9.8f) }
    val infiniteTransition = rememberInfiniteTransition(label = "energy")
    val swing by infiniteTransition.animateFloat(
        initialValue = -0.8f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "swing"
    )

    val pe = (1f - cos(swing)) * gravity * 10f
    val ke = (cos(swing) - cos(0.8f)) * gravity * 10f
    val total = pe + ke

    InteractionContainer(
        title = "Conservation of Energy",
        accent = accent,
        legend = listOf("Potential (PE)" to PowerRed, "Kinetic (KE)" to NeonCyan, "Total Energy" to GhostWhite),
        liveIndexes = listOf(
            "PE:" to "${"%.1f".format(pe)}J",
            "KE:" to "${"%.1f".format(ke)}J",
            "ΣE:" to "${"%.1f".format(total)}J"
        ),
        controls = {
            Text("Gravitational Constant (g)", color = GhostWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = gravity, onValueChange = { gravity = it }, 
                valueRange = 1.6f..20f, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, 30f)
            val length = size.height * 0.6f
            val angle = swing
            val endX = center.x + length * sin(angle)
            val endY = center.y + length * cos(angle)
            
            // Support
            drawLine(Color.Gray, Offset(center.x - 40f, center.y), Offset(center.x + 40f, center.y), strokeWidth = 4f)
            
            // String & Bob
            drawLine(GhostWhite.copy(alpha = 0.4f), center, Offset(endX, endY), strokeWidth = 2f)
            drawCircle(accent, radius = 20f, center = Offset(endX, endY))
            drawCircle(Color.White, radius = 22f, center = Offset(endX, endY), style = Stroke(1f))
            
            // Energy Bars
            val barWidth = 30f
            val maxBarH = 100f
            drawRect(PowerRed.copy(alpha = 0.2f), Offset(20f, size.height - 20f - maxBarH), Size(barWidth, maxBarH))
            drawRect(PowerRed, Offset(20f, size.height - 20f - pe * 2f), Size(barWidth, pe * 2f))
            
            drawRect(NeonCyan.copy(alpha = 0.2f), Offset(60f, size.height - 20f - maxBarH), Size(barWidth, maxBarH))
            drawRect(NeonCyan, Offset(60f, size.height - 20f - ke * 2f), Size(barWidth, ke * 2f))
            
            drawLine(GhostWhite, Offset(15f, size.height - 20f - total * 2f), Offset(95f, size.height - 20f - total * 2f), strokeWidth = 2f)
        }
    }
}

@Composable
fun ReflectionInteraction(accent: Color) {
    var angle by remember { mutableFloatStateOf(45f) }
    
    InteractionContainer(
        title = "Optics: Reflection Law",
        accent = accent,
        legend = listOf("Incident Ray" to accent, "Reflected Ray" to PowerRed, "Normal" to GhostWhite.copy(alpha = 0.3f)),
        liveIndexes = listOf(
            "θi:" to "${angle.toInt()}°",
            "θr:" to "${angle.toInt()}°",
            "Medium:" to "Mirror"
        ),
        controls = {
            Text("Incident Angle θ", color = GhostWhite.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = angle, onValueChange = { angle = it }, 
                valueRange = 5f..85f, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height - 40f)
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val lineLen = min(size.width, size.height) * 0.8f
            
            // Mirror Surface
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray)),
                topLeft = Offset(40f, size.height - 40f),
                size = Size(size.width - 80f, 10f)
            )
            
            // Normal
            drawLine(GhostWhite.copy(alpha = 0.3f), center, Offset(center.x, 40f), strokeWidth = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            
            // Incident Ray
            val ix = center.x - lineLen * sin(rad)
            val iy = center.y - lineLen * cos(rad)
            drawLine(accent, Offset(ix, iy), center, strokeWidth = 4f, cap = StrokeCap.Round)
            
            // Reflected Ray
            val rx = center.x + lineLen * sin(rad)
            val ry = center.y - lineLen * cos(rad)
            drawLine(PowerRed, center, Offset(rx, ry), strokeWidth = 4f, cap = StrokeCap.Round)
            
            // Angle arcs
            drawArc(accent.copy(alpha = 0.2f), 270f - angle, angle, true, Offset(center.x - 30f, center.y - 30f), Size(60f, 60f))
            drawArc(PowerRed.copy(alpha = 0.2f), 270f, angle, true, Offset(center.x - 30f, center.y - 30f), Size(60f, 60f))
        }
    }
}

@Composable
fun HeatTransferInteraction(accent: Color) {
    var conductionMode by remember { mutableStateOf(true) }
    val infiniteTransition = rememberInfiniteTransition(label = "heat")
    val flow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "flow"
    )

    InteractionContainer(
        title = "Thermodynamics: Heat Flow",
        accent = accent,
        legend = listOf("Heat Source" to PowerRed, "Energy Flow" to Color.Yellow, "Medium" to Color.Gray),
        liveIndexes = listOf(
            "Mode:" to if(conductionMode) "Conduction" else "Convection",
            "ΔT:" to "High",
            "Flux:" to "${(flow * 100).toInt()} W/m²"
        ),
        controls = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("CONVECTION", color = if(!conductionMode) NeonCyan else GhostWhite.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Switch(checked = conductionMode, onCheckedChange = { conductionMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = accent))
                Text("CONDUCTION", color = if(conductionMode) accent else GhostWhite.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            
            // Heat Source
            drawCircle(
                brush = Brush.radialGradient(listOf(PowerRed, Color.Transparent), center = Offset(60f, centerY), radius = 60f),
                radius = 40f + flow * 10f,
                center = Offset(60f, centerY)
            )
            
            if(conductionMode) {
                // Metal Rod
                drawRect(Color.Gray, Offset(100f, centerY - 15f), Size(size.width - 160f, 30f))
                for(i in 0..10) {
                    val x = 110f + i * 25f + (flow * 25f)
                    if(x < size.width - 70f) {
                        drawCircle(Color.Yellow.copy(alpha = 0.8f * (1f - x/size.width)), radius = 4f, center = Offset(x, centerY))
                    }
                }
            } else {
                // Fluid Circulation
                for(i in 0..5) {
                    val angle = (flow * 360f + i * 60f).toDouble()
                    val radX = 80f
                    val radY = 40f
                    val cx = size.width / 2f + radX * cos(Math.toRadians(angle)).toFloat()
                    val cy = centerY + radY * sin(Math.toRadians(angle)).toFloat()
                    drawCircle(NeonCyan.copy(alpha = 0.6f), radius = 6f, center = Offset(cx, cy))
                }
                drawRect(NeonCyan.copy(alpha = 0.1f), Offset(size.width/2 - 100f, centerY - 60f), Size(200f, 120f), style = Stroke(2f))
            }
        }
    }
}

@Composable
fun WaveMechanicsInteraction(accent: Color) {
    var freq by remember { mutableFloatStateOf(2f) }
    var amp by remember { mutableFloatStateOf(40f) }
    val time by rememberInfiniteTransition("wave").animateFloat(0f, 2 * PI.toFloat(), infiniteRepeatable(tween(2000, easing = LinearEasing)))

    InteractionContainer(
        title = "Wave Mechanics: Superposition",
        accent = accent,
        legend = listOf("Transverse Wave" to accent, "Nodes" to Color.White, "Energy Path" to accent.copy(alpha = 0.1f)),
        liveIndexes = listOf(
            "Freq:" to "${"%.1f".format(freq)} Hz",
            "λ:" to "${"%.1f".format(400f/freq)} px",
            "Phase:" to "${(time * 180 / PI).toInt()}°"
        ),
        controls = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Frequency", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = freq, onValueChange = { freq = it }, valueRange = 0.5f..5f, colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent))
                }
                Column(Modifier.weight(1f)) {
                    Text("Amplitude", color = PowerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(value = amp, onValueChange = { amp = it }, valueRange = 10f..80f, colors = SliderDefaults.colors(thumbColor = PowerRed, activeTrackColor = PowerRed))
                }
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            val path = Path()
            for (x in 0..size.width.toInt()) {
                val dx = x.toFloat()
                val dy = centerY + amp * sin(dx * 0.05f * freq + time)
                if (x == 0) path.moveTo(dx, dy) else path.lineTo(dx, dy)
                
                // Nodes indicator
                if (abs(dy - centerY) < 1f && x % 50 == 0) {
                    drawCircle(Color.White, radius = 4f, center = Offset(dx, dy))
                }
            }
            drawPath(path, accent, style = Stroke(4f, cap = StrokeCap.Round))
            
            // Crest/Trough markers
            val crestX = (PI.toFloat() * 1.5f - time) / (0.05f * freq)
            if (crestX > 0 && crestX < size.width) {
                drawCircle(PowerRed, radius = 6f, center = Offset(crestX, centerY - amp))
            }
        }
    }
}

@Composable
fun StatesOfMatterInteraction(accent: Color) {
    var temp by remember { mutableFloatStateOf(20f) }
    val infiniteTransition = rememberInfiniteTransition(label = "matter")
    val vibration by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(100, easing = LinearEasing), RepeatMode.Reverse),
        label = "vibration"
    )

    val state = when { temp < 30 -> "SOLID"; temp < 75 -> "LIQUID"; else -> "GAS" }
    val color = when(state) { "SOLID" -> accent; "LIQUID" -> NeonCyan; else -> PowerRed }

    InteractionContainer(
        title = "Molecular Kinetic Theory",
        accent = color,
        legend = listOf("Molecule" to color, "Kinetic Energy" to Color.Yellow, "Collision" to Color.White),
        liveIndexes = listOf(
            "Temp:" to "${temp.toInt()}°C",
            "State:" to state,
            "Ek:" to "${(temp * 1.5).toInt()} meV"
        ),
        controls = {
            Text("Thermal Energy Level", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = temp, onValueChange = { temp = it }, 
                valueRange = 0f..100f, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val rows = if(state == "GAS") 2 else 4
            val cols = if(state == "GAS") 3 else 6
            val spacing = if(state == "GAS") 100f else 40f
            val startX = (size.width - (cols-1)*spacing) / 2
            val startY = (size.height - (rows-1)*spacing) / 2
            val vibScale = (temp / 100f) * (if(state == "GAS") 40f else 10f)
            
            for(r in 0 until rows) {
                for(c in 0 until cols) {
                    val randX = sin((r+c+temp).toDouble()).toFloat() * vibScale
                    val randY = cos((r*c+temp).toDouble()).toFloat() * vibScale
                    val offset = Offset(startX + c * spacing + randX, startY + r * spacing + randY)
                    
                    drawCircle(
                        brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0.4f)), center = offset, radius = 12f),
                        radius = 10f,
                        center = offset
                    )
                    if (temp > 50) {
                        drawCircle(Color.Yellow.copy(alpha = 0.3f), radius = 15f + vibScale, center = offset)
                    }
                }
            }
        }
    }
}

@Composable
fun ElementsMixturesInteraction(accent: Color) {
    var showMixture by remember { mutableStateOf(false) }
    
    InteractionContainer(
        title = "Substances vs Mixtures",
        accent = accent,
        legend = listOf("Element A" to accent, "Element B" to TechViolet, "Bond" to Color.Gray),
        liveIndexes = listOf(
            "Type:" to if(showMixture) "Mixture" else "Pure Element",
            "Homogeneity:" to "High",
            "Phase:" to "Solid"
        ),
        controls = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PURE SUBSTANCE", color = if(!showMixture) accent else GhostWhite.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Switch(checked = showMixture, onCheckedChange = { showMixture = it }, colors = SwitchDefaults.colors(checkedThumbColor = TechViolet))
                Text("HETEROGENEOUS MIXTURE", color = if(showMixture) TechViolet else GhostWhite.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cols = 8
            val rows = 4
            val spacing = 40f
            val startX = (size.width - (cols-1)*spacing) / 2
            val startY = (size.height - (rows-1)*spacing) / 2
            
            for(r in 0 until rows) {
                for(c in 0 until cols) {
                    val pos = Offset(startX + c * spacing, startY + r * spacing)
                    val isTypeB = showMixture && (r + c) % 2 != 0
                    val color = if (isTypeB) TechViolet else accent
                    
                    drawCircle(color, radius = 10f, center = pos)
                    drawCircle(Color.White.copy(alpha = 0.2f), radius = 12f, center = pos, style = Stroke(1f))
                    
                    // Simple bond visual for pure substance
                    if (!showMixture && c < cols - 1) {
                        drawLine(Color.Gray.copy(alpha = 0.3f), pos, Offset(pos.x + spacing, pos.y), strokeWidth = 2f)
                    }
                }
            }
        }
    }
}

@Composable
fun AtomModelInteraction(accent: Color) {
    var protons by remember { mutableIntStateOf(3) }
    val rotation by rememberInfiniteTransition("atom").animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)))

    val elementName = when(protons) { 1 -> "Hydrogen"; 2 -> "Helium"; 3 -> "Lithium"; 4 -> "Beryllium"; 5 -> "Boron"; else -> "Heavier" }

    InteractionContainer(
        title = "Atomic Structure: Bohr Model",
        accent = accent,
        legend = listOf("Proton" to PowerRed, "Neutron" to Color.Gray, "Electron" to NeonCyan),
        liveIndexes = listOf(
            "Element:" to elementName,
            "Z:" to "$protons",
            "Electrons:" to "$protons"
        ),
        controls = {
            Text("Proton Count (Z)", color = PowerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = protons.toFloat(), onValueChange = { protons = it.toInt() }, 
                valueRange = 1f..10f, steps = 8,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = PowerRed)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Nucleus
            repeat(protons) { i ->
                val ang = Math.toRadians((i * (360f/protons)).toDouble()).toFloat()
                val off = Offset(center.x + 6f * cos(ang), center.y + 6f * sin(ang))
                drawCircle(PowerRed, radius = 6f, center = off)
                drawCircle(Color.Gray, radius = 6f, center = Offset(off.x + 4f, off.y + 4f))
            }
            
            // Shells
            val shell1Electrons = min(protons, 2)
            val shell2Electrons = max(0, protons - 2)
            
            // Shell 1
            drawCircle(GhostWhite.copy(alpha = 0.1f), radius = 50f, center = center, style = Stroke(1f))
            repeat(shell1Electrons) { i ->
                val ang = rotation + i * (360f/shell1Electrons)
                val ex = center.x + 50f * cos(Math.toRadians(ang.toDouble())).toFloat()
                val ey = center.y + 50f * sin(Math.toRadians(ang.toDouble())).toFloat()
                drawCircle(NeonCyan, radius = 4f, center = Offset(ex, ey))
                drawCircle(NeonCyan.copy(alpha = 0.3f), radius = 7f, center = Offset(ex, ey))
            }
            
            // Shell 2
            if (protons > 2) {
                drawCircle(GhostWhite.copy(alpha = 0.1f), radius = 90f, center = center, style = Stroke(1f))
                repeat(shell2Electrons) { i ->
                    val ang = -rotation * 0.7f + i * (360f/shell2Electrons)
                    val ex = center.x + 90f * cos(Math.toRadians(ang.toDouble())).toFloat()
                    val ey = center.y + 90f * sin(Math.toRadians(ang.toDouble())).toFloat()
                    drawCircle(NeonCyan, radius = 4f, center = Offset(ex, ey))
                }
            }
        }
    }
}

@Composable
fun ChemicalChangeInteraction(accent: Color) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    InteractionContainer(
        title = "Chemical Reaction: Synthesis",
        accent = accent,
        legend = listOf("Reactant A" to PowerRed, "Reactant B" to NeonCyan, "Product AB" to TechViolet),
        liveIndexes = listOf(
            "Reaction:" to "A + B → AB",
            "Yield:" to "${(progress * 100).toInt()}%",
            "Enthalpy:" to "Exothermic"
        ),
        controls = {
            Text("Reaction Progress", color = GhostWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = progress, onValueChange = { progress = it }, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            
            if (progress < 0.7f) {
                val dist = (1f - progress) * 100f
                // A particles
                repeat(3) { i ->
                    drawCircle(PowerRed, radius = 12f, center = Offset(centerX - dist - 30f, centerY + (i-1)*40f))
                }
                // B particles
                repeat(3) { i ->
                    drawCircle(NeonCyan, radius = 12f, center = Offset(centerX + dist + 30f, centerY + (i-1)*40f))
                }
            } else {
                // Product
                repeat(3) { i ->
                    val py = centerY + (i-1)*40f
                    drawCircle(TechViolet, radius = 18f, center = Offset(centerX, py))
                    drawCircle(Color.White.copy(alpha = 0.3f), radius = 22f, center = Offset(centerX, py), style = Stroke(2f))
                }
                // Energy release (sparkles)
                repeat(8) { i ->
                    val ang = Math.toRadians((i * 45f + progress * 100f).toDouble()).toFloat()
                    val rx = centerX + 60f * cos(ang)
                    val ry = centerY + 60f * sin(ang)
                    drawCircle(Color.Yellow, radius = 2f, center = Offset(rx, ry))
                }
            }
        }
    }
}

@Composable
fun AcidsBasesInteraction(accent: Color) {
    var ph by remember { mutableFloatStateOf(7f) }
    val phColor = when { 
        ph < 3f -> Color.Red 
        ph < 6f -> Color(0xFFFFA500) 
        ph < 8f -> Color.Green 
        ph < 11f -> Color.Blue 
        else -> TechViolet 
    }

    InteractionContainer(
        title = "pH Scale & Indicators",
        accent = phColor,
        legend = listOf("Indicator Color" to phColor, "Neutral (pH 7)" to Color.Green, "Acid/Base Shift" to Color.White),
        liveIndexes = listOf(
            "pH:" to "%.1f".format(ph),
            "Nature:" to when { ph < 6.5 -> "Acidic"; ph > 7.5 -> "Alkaline"; else -> "Neutral" },
            "[H+]:" to "10^-${ph.toInt()} M"
        ),
        controls = {
            Text("Adjust pH Level", color = phColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = ph, onValueChange = { ph = it }, valueRange = 0f..14f,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = phColor, activeTrackColor = phColor)
            )
        }
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                Modifier.size(120.dp).clip(CircleShape).background(phColor.copy(alpha = 0.2f)).border(4.dp, phColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("%.1f".format(ph), color = phColor, fontSize = 40.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(20.dp))
            // Scale visual
            Canvas(Modifier.fillMaxWidth().height(20.dp).padding(horizontal = 40.dp)) {
                val brush = Brush.horizontalGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, TechViolet))
                drawRoundRect(brush, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f))
                val markerX = (ph / 14f) * size.width
                drawCircle(Color.White, radius = 6f, center = Offset(markerX, size.height/2))
            }
        }
    }
}

@Composable
fun QuantumAtomicInteraction(accent: Color) {
    var n by remember { mutableIntStateOf(1) }
    val time by rememberInfiniteTransition("quantum").animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)))

    InteractionContainer(
        title = "Quantum Atomic Theory",
        accent = accent,
        legend = listOf("Probability Cloud" to accent, "Nucleus" to PowerRed, "Node" to GhostWhite),
        liveIndexes = listOf(
            "Level (n):" to "$n",
            "Orbital:" to when(n) { 1 -> "1s"; 2 -> "2s"; 3 -> "3s"; else -> "ns" },
            "ψ²:" to "Probabilistic"
        ),
        controls = {
            Text("Principal Quantum Number (n)", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = n.toFloat(), onValueChange = { n = it.toInt() }, valueRange = 1f..4f, steps = 2,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(PowerRed, radius = 8f, center = center)
            
            // Draw Probability Cloud
            repeat(1000) { i ->
                val seed = i.toDouble()
                val distBase = n * 40f
                // Distribution based on quantum level
                val r = (distBase + sin(seed * 0.1).toFloat() * 10f) * (1f + 0.2f * sin(seed * 0.5 + time.toDouble() * 10.0).toFloat())
                val ang = seed * 137.5 // Golden angle for even distribution
                val px = center.x + r * cos(Math.toRadians(ang)).toFloat()
                val py = center.y + r * sin(Math.toRadians(ang)).toFloat()
                
                drawCircle(accent.copy(alpha = 0.05f), radius = 2f, center = Offset(px, py))
            }
        }
    }
}

@Composable
fun CellInteraction(accent: Color) {
    var selectedOrganelle by remember { mutableStateOf("Nucleus") }
    val pulse by rememberInfiniteTransition("cell").animateFloat(1f, 1.05f, infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse))

    InteractionContainer(
        title = "The Living Cell",
        accent = BioLime,
        legend = listOf("Membrane" to BioLime, "Nucleus" to TechViolet, "Mitochondria" to PowerRed),
        liveIndexes = listOf(
            "Selected:" to selectedOrganelle,
            "Activity:" to "Metabolic",
            "ATP Level:" to "Optimal"
        ),
        controls = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                OrganelleChip("Nucleus", selectedOrganelle == "Nucleus") { selectedOrganelle = it }
                Spacer(Modifier.width(8.dp))
                OrganelleChip("Mitochondria", selectedOrganelle == "Mitochondria") { selectedOrganelle = it }
                Spacer(Modifier.width(8.dp))
                OrganelleChip("Ribosomes", selectedOrganelle == "Ribosomes") { selectedOrganelle = it }
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseR = 80f * pulse
            
            // Membrane
            drawCircle(BioLime.copy(alpha = 0.1f), radius = baseR, center = center)
            drawCircle(BioLime, radius = baseR, center = center, style = Stroke(4f))
            
            // Nucleus
            val nColor = if(selectedOrganelle == "Nucleus") TechViolet else TechViolet.copy(alpha = 0.6f)
            drawCircle(nColor, radius = baseR * 0.4f, center = center)
            drawCircle(Color.Black.copy(alpha = 0.3f), radius = baseR * 0.15f, center = center)
            
            // Mitochondria
            repeat(3) { i ->
                val ang = i * 120f + 45f
                val mx = center.x + baseR * 0.65f * cos(Math.toRadians(ang.toDouble())).toFloat()
                val my = center.y + baseR * 0.65f * sin(Math.toRadians(ang.toDouble())).toFloat()
                val mColor = if(selectedOrganelle == "Mitochondria") PowerRed else PowerRed.copy(alpha = 0.6f)
                drawOval(mColor, topLeft = Offset(mx-15f, my-8f), size = Size(30f, 16f))
            }
        }
    }
}

@Composable
fun PlantTissueInteraction(accent: Color) {
    var mode by remember { mutableStateOf("Xylem") }
    val flow by rememberInfiniteTransition("tissue").animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)))

    InteractionContainer(
        title = "Vascular Transport",
        accent = accent,
        legend = listOf("Vessel" to Color.Gray, "Nutrients" to NeonCyan, "Flow Dir" to Color.White),
        liveIndexes = listOf(
            "Tissue:" to mode,
            "Transport:" to if(mode == "Xylem") "Water/Minerals" else "Sucrose/Food",
            "Direction:" to if(mode == "Xylem") "Unidirectional" else "Bidirectional"
        ),
        controls = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { mode = "Xylem" }, 
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if(mode == "Xylem") accent else CardBg)
                ) { Text("XYLEM", fontSize = 12.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { mode = "Phloem" }, 
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if(mode == "Phloem") accent else CardBg)
                ) { Text("PHLOEM", fontSize = 12.sp, fontWeight = FontWeight.Black) }
            }
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val spacing = 60f
            repeat(4) { i ->
                val x = 100f + i * spacing
                // Vessel walls
                drawLine(Color.Gray, Offset(x - 15f, 20f), Offset(x - 15f, size.height - 20f), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(x + 15f, 20f), Offset(x + 15f, size.height - 20f), strokeWidth = 2f)
                
                // Flow
                repeat(5) { j ->
                    val y = ((j * 50f + flow * 200f) % (size.height - 40f)) + 20f
                    val actualY = if(mode == "Xylem") size.height - y else y
                    drawCircle(if(mode == "Xylem") NeonCyan else Color.Yellow, radius = 4f, center = Offset(x, actualY))
                }
            }
        }
    }
}

@Composable
fun NutritionInteraction(accent: Color) {
    var lightIntensity by remember { mutableFloatStateOf(50f) }
    
    InteractionContainer(
        title = "Photosynthesis: Glucose Synthesis",
        accent = BioLime,
        legend = listOf("Chloroplast" to BioLime, "Light Energy" to Color.Yellow, "Glucose" to Color.White),
        liveIndexes = listOf(
            "Light:" to "${lightIntensity.toInt()}%",
            "CO2 Fix:" to "Active",
            "Rate:" to "${(lightIntensity * 0.8).toInt()} μmol/s"
        ),
        controls = {
            Text("Light Source Intensity", color = Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = lightIntensity, onValueChange = { lightIntensity = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = Color.Yellow)
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Leaf cell
            drawRoundRect(BioLime.copy(alpha = 0.1f), Offset(center.x - 100f, center.y - 60f), Size(200f, 120f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f))
            
            // Chloroplasts
            repeat(6) { i ->
                val ox = center.x - 70f + (i % 3) * 60f
                val oy = center.y - 30f + (i / 3) * 60f
                drawOval(BioLime, Offset(ox-20f, oy-10f), Size(40f, 20f))
            }
            
            // Light rays
            repeat(5) { i ->
                val lx = 40f + i * 80f
                val alpha = (lightIntensity / 100f) * 0.5f
                drawLine(Color.Yellow.copy(alpha = alpha), Offset(lx, 0f), Offset(lx - 40f, 100f), strokeWidth = 4f)
            }
            
            // Glucose production
            if (lightIntensity > 20f) {
                repeat(10) { i ->
                    val gx = (System.currentTimeMillis() / 20 + i * 40) % 200f + (center.x - 100f)
                    val gy = center.y + sin(gx * 0.1f).toFloat() * 20f
                    if (gx < center.x + 100f) drawCircle(Color.White, radius = 3f, center = Offset(gx, gy))
                }
            }
        }
    }
}

@Composable
fun RespirationInteraction(accent: Color) {
    val expansion by rememberInfiniteTransition("resp").animateFloat(0.8f, 1.2f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse))

    InteractionContainer(
        title = "Cellular Respiration",
        accent = accent,
        legend = listOf("Mitochondria" to accent, "Oxygen (O2)" to NeonCyan, "Energy (ATP)" to Color.Yellow),
        liveIndexes = listOf(
            "Phase:" to "Aerobic",
            "O2 Intake:" to "Optimal",
            "ATP Yield:" to "38 units"
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Mitochondria structure
            val baseR = 70f * expansion
            drawOval(accent.copy(alpha = 0.2f), Offset(center.x - baseR, center.y - baseR/2), Size(baseR*2, baseR))
            drawOval(accent, Offset(center.x - baseR, center.y - baseR/2), Size(baseR*2, baseR), style = Stroke(3f))
            
            // Inner membrane folds (Cristae)
            val path = Path().apply {
                moveTo(center.x - baseR + 20f, center.y)
                for(i in 1..8) {
                    val px = center.x - baseR + 20f + i * (baseR*2 - 40f)/8
                    val py = center.y + (if(i % 2 == 0) -20f else 20f) * expansion
                    lineTo(px, py)
                }
            }
            drawPath(path, accent.copy(alpha = 0.5f), style = Stroke(2f))
            
            // ATP release
            repeat(12) { i ->
                val ang = i * 30f + System.currentTimeMillis() * 0.1f
                val dist = 90f * expansion
                val ax = center.x + dist * cos(Math.toRadians(ang.toDouble())).toFloat()
                val ay = center.y + dist * sin(Math.toRadians(ang.toDouble())).toFloat()
                drawCircle(Color.Yellow, radius = 4f, center = Offset(ax, ay))
            }
        }
    }
}

@Composable
fun PlantReproductionInteraction(accent: Color) {
    val pollenPos by rememberInfiniteTransition("pollin").animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing)))

    InteractionContainer(
        title = "Plant Reproduction: Pollination",
        accent = accent,
        legend = listOf("Stigma" to accent, "Pollen Grain" to Color.Yellow, "Pollen Tube" to NeonCyan),
        liveIndexes = listOf(
            "Status:" to if(pollenPos > 0.8f) "Fertilized" else "In-Progress",
            "Vector:" to "Wind/Insect",
            "Zygote:" to if(pollenPos > 0.9f) "Forming" else "None"
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Stigma & Style
            drawRect(accent.copy(alpha = 0.3f), Offset(center.x - 10f, center.y), Size(20f, 80f))
            drawCircle(accent, radius = 25f, center = center)
            
            // Pollen flight
            val px = 40f + (center.x - 40f) * pollenPos
            val py = 60f + (center.y - 60f) * pollenPos - sin(pollenPos * PI.toFloat()) * 80f
            
            drawCircle(Color.Yellow, radius = 8f, center = Offset(px, py))
            drawCircle(Color.White.copy(alpha = 0.4f), radius = 12f, center = Offset(px, py), style = Stroke(1f))
            
            // Pollen Tube formation
            if (pollenPos > 0.5f) {
                val tubeProgress = (pollenPos - 0.5f) * 2f
                drawLine(NeonCyan, center, Offset(center.x, center.y + 60f * tubeProgress), strokeWidth = 4f, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
fun OrganelleChip(name: String, isSelected: Boolean, onClick: (String) -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = { onClick(name) },
        label = { Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TechViolet.copy(alpha = 0.2f),
            selectedLabelColor = TechViolet,
            containerColor = CardBg,
            labelColor = GhostWhite.copy(alpha = 0.6f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            selected = isSelected,
            enabled = true,
            borderColor = GhostWhite.copy(alpha = 0.1f),
            selectedBorderColor = TechViolet
        )
    )
}

@Composable
fun DefaultPlaceholder(topic: TopicDetail, accent: Color) {
    InteractionContainer(
        title = topic.title,
        accent = accent,
        legend = listOf("Conceptual Data" to accent),
        liveIndexes = listOf("Status:" to "Ready")
    ) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            FlaskIcon(color = accent, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text("INTERACTIVE MODULE ACTIVE", color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}
