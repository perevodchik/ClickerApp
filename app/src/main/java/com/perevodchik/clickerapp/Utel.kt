package com.perevodchik.clickerapp

import kotlin.math.roundToInt


fun toPercentage(var0: Int, var1: Int): Float {
    return (var0.toFloat() / var1) * 100
}

fun fromPercentage(var0: Float, var1: Int): Float {
    return (var0 / 100) * var1
}

fun toPercentage0(var0: Int, var1: Int): Float {
    return (var0.toFloat() / var1) * 100
}

fun fromPercentage0(var0: Float, var1: Int): Int {
    return ((var0 / 100) * var1).roundToInt()
}

fun validValue(var0: Float): Float {
    return if(var0 < 0) 0F else if(var0 > 100) 100F else var0
}

fun validValue(var0: Int): Int {
    return if(var0 < 0) 0 else if(var0 > 100) 100 else var0
}


/*


val url = «http» + ":" + "/" + "/"
    -> http://


loop {
    if (lastchar (url)="/")
        url = url + «bklang»
        if (lenght(url) < 14)
            url = url + "." + «net»
        else {
            url = url + "/a" leave;
        }
}

url = url + «xu92h»


 */