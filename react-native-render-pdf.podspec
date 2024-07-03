require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

fabric_enabled = ENV['RCT_NEW_ARCH_ENABLED'] == '1'

Pod::Spec.new do |s|
  s.name           = package['name']
  s.version        = package['version']
  s.summary        = package['summary']
  s.description    = package['description']
  s.author         = package['author']['name']
  s.license        = package['license']
  s.homepage       = package['homepage']
  s.source         = { :git => 'https://github.com/danfordgidraph/react-native-render-pdf.git', :tag => "v#{s.version}" }
  s.requires_arc   = true
  s.framework    = "PDFKit"

  if fabric_enabled
    folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'

    s.pod_target_xcconfig = {
      'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/boost" "$(PODS_ROOT)/boost-for-react-native"  "$(PODS_ROOT)/RCT-Folly"',
      "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    }
    s.platforms       = { ios: '13.0', tvos: '13.0' }
    s.compiler_flags  = folly_compiler_flags + ' -DRCT_NEW_ARCH_ENABLED'
    s.source_files    = 'ios/**/*.{h,m,mm,cpp}'
    s.requires_arc    = true

    s.dependency "React"
    s.dependency "React-RCTFabric"
    s.dependency "React-Codegen"
    s.dependency "RCT-Folly"
    s.dependency "RCTRequired"
    s.dependency "RCTTypeSafety"
    s.dependency "ReactCommon/turbomodule/core"
  else
    s.platform       = :ios, '8.0'
    s.source_files   = 'ios/**/*.{h,m,mm}'
    s.dependency     'React-Core'
  end
end
