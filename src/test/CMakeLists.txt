include_directories("../")

if (WIN32)

include_directories("C:\\cxxtest")

endif (WIN32)

add_executable(cxxtest 
   main_cxxtest.cpp 
   FlashTestSuite.cpp
   CoreTestSuite.cpp
   MacTestSuite.cpp)

target_link_libraries(cxxtest sikuli 
				${APPLE_LIBRARIES} 
				${OPENCV_LIBRARIES})


