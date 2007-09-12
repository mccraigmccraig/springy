
# just a disposer
static_bean "springy.beans.Bean7" do |i,d|
    d.disposerMethod
end

bean :bean6, "springy.beans.Bean6"

bean :bean8, "springy.beans.Bean8" do |b|
    b.new( :parent_bean )
end
