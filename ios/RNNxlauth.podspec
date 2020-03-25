
Pod::Spec.new do |s|
  s.name         = "RNNxlauth"
  s.version      = "1.0.0"
  s.summary      = "RNNxlauth"
  s.description  = <<-DESC
                  RNNxlauth
                   DESC
  s.homepage     = "https://github.com/digitalme-my/react-native-nxlauth"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNNxlauth.git", :tag => "master" }
  s.source_files  = "RNNxlauth/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  s.dependency "AppAuth"
  #s.dependency "others"

end

  
