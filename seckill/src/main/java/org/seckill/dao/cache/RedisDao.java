package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by zhengfucheng on 8/2/2017.
 */
public class RedisDao {

    private JedisPool jedisPool;


    public RedisDao(String ip, int port) {
        this.jedisPool = new JedisPool(ip, port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId) {

        try {
            Jedis jedis = jedisPool.getResource();
//            jedis.flushDB();

            try {
                String key = "seckill:" + seckillId;
                //并没有实现内部序列化操作
                //get->byte[]->反序列化->object(seckill)

                //protostuff: pojo
                byte[] bytes = jedis.get(key.getBytes());

                if (bytes != null) {
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);

                    return seckill;
                }


            } finally {
                jedis.close();
            }


        } catch (Exception e) {

        }


        return null;
    }


    public String putSeckill(Seckill seckill) {
        //set object(seckill)->序列化->bytes[]

        try {
            Jedis jedis = jedisPool.getResource();

            try {
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,
                        schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            }finally {
                jedis.close();
            }

        } catch (Exception e) {

        }

        return null;
    }
}
