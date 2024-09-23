require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))
folly_version = '2021.07.22.00'
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'

Pod::Spec.new do |s|

  # This guard prevent to install the dependencies when we run `pod install` in the old architecture.

  
  s.name           = "react-native-contacts"
  s.version        = package["version"]
  s.summary        = package["description"]
  s.homepage       = "https://github.com/geektimecoil/react-native-onesignal"
  s.license        = package["license"]
  s.author         = { package["author"] => package["author"] }
  s.platform       = :ios, "12.0"
  s.source         = { :git => "https://github.com/rt2zz/react-native-contacts.git", :tag => "v#{s.version}" } 
  s.source_files   = "ios/**/*.{h,m,mm,swift}"
  s.frameworks     = 'Contacts', 'ContactsUI', 'Photos'

  s.dependency 'React-Core'

  if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
    s.compiler_flags = folly_compiler_flags + " -DRCT_NEW_ARCH_ENABLED=1"
    s.pod_target_xcconfig    = {
      "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\"",
      "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
      "CLANG_CXX_LANGUAGE_STANDARD" => "c++17"
    }
    s.dependency "React-Codegen"
    s.dependency "RCT-Folly", folly_version
    s.dependency "RCTRequired"
    s.dependency "RCTTypeSafety"
    s.dependency "ReactCommon/turbomodule/core"
    install_modules_dependencies(s)
  end
end
