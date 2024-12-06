package com.example.mhnfe.domain.ai.yolo

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil

object InterpreterInitializer {

    fun createInterpreter(context: Context, modelPath: String, useGpu: Boolean): Interpreter {
        val options = Interpreter.Options().apply {
            if (useGpu) {
                val compatList = CompatibilityList()
                if (compatList.isDelegateSupportedOnThisDevice) {
                    val delegateOptions = compatList.bestOptionsForThisDevice
                    this.addDelegate(GpuDelegate(delegateOptions))
                } else {
                    this.setNumThreads(4)
                }
            } else {
                this.setNumThreads(4)
            }
        }

        val model = FileUtil.loadMappedFile(context, modelPath)
        return Interpreter(model, options)
    }
}
