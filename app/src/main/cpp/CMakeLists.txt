# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-exceptions -fno-rtti")

cmake_minimum_required(VERSION 3.4.1)

add_library(cymoe SHARED cymoe.cpp dlopen.c)

target_include_directories(cymoe PRIVATE include)

add_subdirectory(substrate)

target_link_libraries(cymoe substrate android log)
