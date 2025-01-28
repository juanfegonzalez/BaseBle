package com.indusair.basebluetooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.indusair.basebluetooth.bleapplication.BluetoothViewModel
import com.indusair.basebluetooth.ui.theme.BaseBluetoothTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var viewModel: BluetoothViewModel = BluetoothViewModel(application)
        setContent {
            BaseBluetoothTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        Text("Hola mundo")
                        viewModel.connectToDevice( "A0:A3:B3:2B:09:72")
                    }
                }
            }
        }
    }
}
